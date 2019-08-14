/*
 * Copyright 2014-2019 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.scheduler.scaling;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.deployment.scheduler.exceptions.ProcessDeletionException;
import io.github.cloudiator.deployment.scheduler.instantiation.AutomaticInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.DependencyGraph;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationException;
import io.github.cloudiator.deployment.scheduler.instantiation.MatchmakingEngine;
import io.github.cloudiator.deployment.scheduler.instantiation.ResourcePool;
import io.github.cloudiator.deployment.scheduler.instantiation.TaskInterfaceSelection;
import io.github.cloudiator.deployment.scheduler.processes.ProcessKiller;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cloudiator.messages.Process.CreateSparkClusterRequest;
import org.cloudiator.messages.Process.SparkClusterCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.Nodes;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScalingEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScalingEngine.class);
  private final AutomaticInstantiationStrategy automaticInstantiationStrategy;
  private final MatchmakingEngine matchmakingEngine;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final NodeMessageRepository nodeMessageRepository;
  private final ResourcePool resourcePool;
  private final ProcessDomainRepository processDomainRepository;
  private final ProcessService processService;
  private final ProcessKiller processKiller;

  @Inject
  public ScalingEngine(
      AutomaticInstantiationStrategy automaticInstantiationStrategy,
      MatchmakingEngine matchmakingEngine,
      ScheduleDomainRepository scheduleDomainRepository,
      NodeMessageRepository nodeMessageRepository,
      ResourcePool resourcePool,
      ProcessDomainRepository processDomainRepository,
      ProcessService processService,
      ProcessKiller processKiller) {
    this.automaticInstantiationStrategy = automaticInstantiationStrategy;
    this.matchmakingEngine = matchmakingEngine;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.nodeMessageRepository = nodeMessageRepository;
    this.resourcePool = resourcePool;
    this.processDomainRepository = processDomainRepository;
    this.processService = processService;
    this.processKiller = processKiller;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule refresh(Schedule schedule) {
    return scheduleDomainRepository
        .findByIdAndUser(schedule.id(), schedule.userId());
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  CloudiatorClusterProcess save(CloudiatorClusterProcess cloudiatorClusterProcess) {
    return (CloudiatorClusterProcess) processDomainRepository.save(cloudiatorClusterProcess);
  }

  private Schedule checkSchedule(Schedule schedule) {
    final Schedule refresh = refresh(schedule);

    if (!(refresh.state().equals(ScheduleState.RUNNING) || refresh.state()
        .equals(ScheduleState.MANUAL))) {
      throw new IllegalStateException(
          String.format(
              "Schedule %s is currently in a state transition (%s). Scaling is not allowed.",
              schedule, schedule.state()));
    }

    return refresh;
  }


  private final List<ListenableFuture<Node>> nodesToFutures(Collection<? extends Node> nodes) {

    List<ListenableFuture<Node>> nodeFutures = new ArrayList<>(nodes.size());
    for (Node node : nodes) {
      SettableFuture<Node> settableFuture = SettableFuture.create();
      settableFuture.set(node);
      nodeFutures.add(settableFuture);
    }

    return nodeFutures;
  }

  public void scaleOut(Schedule schedule, Job job, Task task, Collection<? extends Node> nodes)
      throws MatchmakingException, InstantiationException {

    LOGGER.info(String
        .format("Scale Out. Schedule: %s, Job: %s, Task: %s, Nodes: %s", schedule, job, task,
            nodes));

    if (nodes.isEmpty()) {
      scaleOutWithoutNodes(schedule, job, task);
    } else {
      scaleOutWithNodes(schedule, job, task, nodes);
    }
  }

  public void scaleIn(Schedule schedule, Job job, Task task, Collection<? extends Node> nodes) {
    schedule = checkSchedule(schedule);

    LOGGER.info(String
        .format("Scale In. Schedule: %s, Job: %s, Task: %s, Nodes: %s", schedule, job, task,
            nodes));

    if (nodes.isEmpty()) {
      scaleInWithoutNodes(schedule, job, task);
    } else {
      scaleInWithNodes(schedule, task, nodes);
    }
  }

  private void scaleInWithNodes(Schedule schedule, Task task,
      Collection<? extends Node> nodes) {

    checkState(schedule.instantiation().equals(Instantiation.MANUAL),
        "Scaling with nodes attached is only allowed for MANUAL instantiation.");

    scaleInInternally(schedule, task, nodes);

  }

  private void scaleInWithoutNodes(Schedule schedule, Job job, Task task) {
    throw new UnsupportedOperationException();
  }

  private void scaleInInternally(Schedule schedule, Task task, Collection<? extends Node> nodes) {

    Set<CloudiatorProcess> affectedProcesses = new HashSet<>();

    for (Node node : nodes) {
      affectedProcesses.addAll(schedule.processesForNode(node));
    }

    if (affectedProcesses.size() == 1) {
      CloudiatorProcess process = affectedProcesses.iterator().next();
      if (process instanceof CloudiatorClusterProcess) {
        scaleInCluster((CloudiatorClusterProcess) process, nodes);
      } else {
        throw new IllegalStateException();
      }
    } else {
      for (CloudiatorProcess affectedProcess : affectedProcesses) {
        checkState(affectedProcess instanceof CloudiatorSingleProcess);
      }
      scaleInSingle(schedule, nodes);
    }
  }

  private void scaleInSingle(Schedule schedule,
      Collection<? extends Node> nodes) {

    Set<CloudiatorProcess> affectedProcesses = new HashSet<>();

    for (Node node : nodes) {
      checkArgument(schedule.runsOnNode(node),
          String.format("Schedule does not have node %s", schedule));
      affectedProcesses.addAll(schedule.processesForNode(node));
    }

    for (CloudiatorProcess affectedProcess : affectedProcesses) {
      deleteProcess(affectedProcess);
    }

    for (Node node : nodes) {
      deleteNode(node.userId(), node.id());
    }


  }

  private void scaleInCluster(CloudiatorClusterProcess cloudiatorClusterProcess,
      Collection<? extends Node> nodes) {

    LOGGER.debug(
        String.format("Scaling in Cluster %s with nodes %s", cloudiatorClusterProcess, nodes));

    for (Node node : nodes) {
      deleteNode(node.userId(), node.id());
    }

    CloudiatorClusterProcess changedProcess = CloudiatorClusterProcessBuilder
        .of(cloudiatorClusterProcess)
        .removeAllNodes(nodes.stream().map(Identifiable::id).collect(
            Collectors.toSet())).build();

    save(changedProcess);

  }

  private void deleteNode(String userId, String nodeId) {
    try {
      nodeMessageRepository.delete(userId, nodeId).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private void deleteProcess(CloudiatorProcess cloudiatorProcess) {
    try {
      processKiller.kill(cloudiatorProcess);
    } catch (ProcessDeletionException e) {
      throw new RuntimeException(e);
    }
  }


  private void scaleOutWithoutNodes(Schedule schedule, Job job, Task task)
      throws MatchmakingException, InstantiationException {

    final Schedule checkedSchedule = checkSchedule(schedule);

    checkArgument(schedule.instantiation().equals(Instantiation.AUTOMATIC),
        "Schedule needs to be automatic to scale without supplying nodes.");

    LOGGER.info(String
        .format("Executing scale out for task %s of job %s in schedule %s.", task, job, schedule));

    //get existing processes
    final Set<CloudiatorProcess> cloudiatorProcesses = schedule.processesForTask(task);

    //get existing nodes
    final Set<Node> nodes = cloudiatorProcesses.stream()
        .flatMap(
            (Function<CloudiatorProcess, Stream<String>>) cloudiatorProcess -> cloudiatorProcess
                .nodes().stream()).map(
            s -> {
              final Node node = nodeMessageRepository.getById(checkedSchedule.userId(), s);
              if (node == null) {
                throw new IllegalStateException(String.format("Node with id %s does not exist", s));
              }
              return node;
            }).collect(Collectors.toSet());

    //perform matchmaking
    final List<NodeCandidate> matchmaking = matchmakingEngine
        .matchmaking(task.requirements(job), nodes, nodes.size() + 1, checkedSchedule.userId());

    //allocate the resources
    final List<ListenableFuture<Node>> allocate = resourcePool
        .allocate(schedule, matchmaking, nodes, task.name());

    scaleOutInternally(schedule, task, allocate);
  }

  private void scaleOutWithNodes(Schedule schedule, Job job, Task task,
      Collection<? extends Node> nodes)
      throws InstantiationException {

    checkArgument(!nodes.isEmpty(), "Supplied nodes are empty.");

    checkArgument(schedule.instantiation().equals(Instantiation.MANUAL),
        "Supplying nodes is not supported for AUTOMATIC instantiation.");

    LOGGER.info(String
        .format("Executing scale for task %s of job %s in schedule %s on nodes %s.", task, job,
            schedule, nodes));

    scaleOutInternally(schedule, task, nodesToFutures(nodes));

  }

  private void scaleOutInternally(Schedule schedule, Task task,
      Collection<ListenableFuture<Node>> nodes) throws InstantiationException {

    TaskInterfaceSelection taskInterfaceSelection = new TaskInterfaceSelection();
    final TaskInterface taskInterface = taskInterfaceSelection.select(task);

    switch (taskInterface.processMapping()) {
      case CLUSTER:
        scaleOutCluster(schedule, task, nodes);
        break;
      case SINGLE:
        scaleOutSingle(schedule, task, taskInterface, nodes);
        break;
      default:
        throw new AssertionError("Unknown process mapping " + taskInterface.processMapping());
    }

  }

  private Collection<CloudiatorProcess> scaleOutCluster(Schedule schedule, Task task,
      Collection<ListenableFuture<Node>> nodes) throws InstantiationException {

    final Set<CloudiatorProcess> cloudiatorProcesses = schedule.processesForTask(task);

    checkState(cloudiatorProcesses.size() == 1,
        "Expected exactly one process for task %s in schedule %s. Found %s,", task, schedule,
        cloudiatorProcesses.size()
    );

    LOGGER.debug(String.format("Scaling OUT Cluster for task %s with nodes %s.", task, nodes));

    CloudiatorProcess cloudiatorProcess = cloudiatorProcesses.iterator().next();

    try {
      final List<Node> startedNodes = Futures.successfulAsList(nodes).get();

      //scale the cluster

      final CreateSparkClusterRequest sparkClusterRequest = CreateSparkClusterRequest.newBuilder()
          .setUserId(schedule.userId()).setNodes(
              Nodes.newBuilder().addAllNodes(startedNodes.stream().map(
                  NodeToNodeMessageConverter.INSTANCE).collect(Collectors.toSet())).build())
          .build();

      final SettableFutureResponseCallback<SparkClusterCreatedResponse, SparkClusterCreatedResponse> settableFutureResponseCallback = SettableFutureResponseCallback
          .create();

      processService.createSparkClusterAsync(sparkClusterRequest, settableFutureResponseCallback);

      settableFutureResponseCallback.get();

      final CloudiatorClusterProcess modifiedProcess = CloudiatorClusterProcessBuilder
          .of((CloudiatorClusterProcess) cloudiatorProcess)
          .addAllNodes(startedNodes.stream().map(
              Identifiable::id).collect(Collectors.toSet())).build();

      final CloudiatorClusterProcess save = save(modifiedProcess);

      LOGGER.info(
          String.format("Scaled out cluster for task %s. Updated process is %s.", task, save));

      return Collections.singleton(save);

    } catch (InterruptedException | ExecutionException e) {
      throw new InstantiationException("Error while scaling cluster.", e);
    }
  }

  private Collection<CloudiatorProcess> scaleOutSingle(Schedule schedule, Task task,
      TaskInterface taskInterface,
      Collection<ListenableFuture<Node>> nodes) throws InstantiationException {

    LOGGER.debug(String.format("Scaling single task %s with nodes %s.", task, nodes));

    final Future<Collection<CloudiatorProcess>> processFutures = automaticInstantiationStrategy
        .deployTask(task, taskInterface, schedule, nodes,
            DependencyGraph.noDependencies(task));

    try {
      final Collection<CloudiatorProcess> cloudiatorProcesses = processFutures.get();

      LOGGER.info(
          String.format("Scaled task %s. Added the processes %s.", task, cloudiatorProcesses));

      return cloudiatorProcesses;

    } catch (InterruptedException | ExecutionException e) {
      throw new InstantiationException("Error while scaling task " + task, e);
    }
  }


}
