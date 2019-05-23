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

package io.github.cloudiator.deployment.scheduler.failure;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.CloudiatorFutures;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.deployment.scheduler.instantiation.DependencyGraph;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationException;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategySelector;
import io.github.cloudiator.deployment.scheduler.instantiation.MatchmakingEngine;
import io.github.cloudiator.deployment.scheduler.instantiation.ResourcePool;
import io.github.cloudiator.deployment.scheduler.instantiation.TaskInterfaceSelection;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messages.Process.ProcessDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleRestore {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleRestore.class);

  private final ResourcePool resourcePool;
  private final JobMessageRepository jobMessageRepository;
  private final NodeMessageRepository nodeMessageRepository;
  private final InstantiationStrategySelector instantiationStrategySelector;
  private final ProcessService processService;
  private final NodeService nodeService;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final MatchmakingEngine matchmakingEngine;

  @Inject
  public ScheduleRestore(ResourcePool resourcePool,
      JobMessageRepository jobMessageRepository,
      NodeMessageRepository nodeMessageRepository,
      InstantiationStrategySelector instantiationStrategySelector,
      ProcessService processService, NodeService nodeService,
      ScheduleDomainRepository scheduleDomainRepository,
      MatchmakingEngine matchmakingEngine) {
    this.resourcePool = resourcePool;
    this.jobMessageRepository = jobMessageRepository;
    this.nodeMessageRepository = nodeMessageRepository;
    this.instantiationStrategySelector = instantiationStrategySelector;
    this.processService = processService;
    this.nodeService = nodeService;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.matchmakingEngine = matchmakingEngine;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule findByIdAndUser(String scheduleId, String userId) {
    return scheduleDomainRepository.findByIdAndUser(scheduleId, userId);
  }

  private Job findJobForSchedule(Schedule schedule) {
    final Job job = jobMessageRepository.getById(schedule.userId(), schedule.job());

    checkState(job != null, String
        .format("Job %s referenced by schedule %s does not longer exist", schedule.job(),
            schedule));
    return job;
  }

  private boolean requiresRestore(Job job, Schedule schedule, CloudiatorProcess cloudiatorProcess) {

    if (!cloudiatorProcess.state().equals(ProcessState.ERROR)) {
      return false;
    }

    final Task task = job.getTask(cloudiatorProcess.taskId())
        .orElseThrow(() -> new IllegalStateException(String
            .format("Task referenced by process %s does not longer exist in job %s.",
                cloudiatorProcess,
                job)));

    if (task.behaviour() instanceof PeriodicBehaviour) {
      return false;
    }

    return true;
  }

  public Schedule heal(Schedule schedule) throws InstantiationException {

    final Job job = findJobForSchedule(schedule);

    Set<CloudiatorProcess> processToBeCleaned = new HashSet<>();
    Set<Node> nodesToBeCleaned = new HashSet<>();
    List<Future<Collection<CloudiatorProcess>>> futures = new LinkedList<>();

    final Map<Task, TaskInterface> taskInterfaceSelection = new TaskInterfaceSelection()
        .select(job);
    final DependencyGraph dependencyGraph = DependencyGraph.of(job, taskInterfaceSelection);

    for (Task task : job.tasks()) {

      Set<Node> reusableNodes = new LinkedHashSet<>();

      for (CloudiatorProcess process : processesPerTask(schedule, task)) {

        if (requiresRestore(job, schedule, process)) {
          processToBeCleaned.add(process);
          nodesToBeCleaned.addAll(getNodes(process));
        } else {
          reusableNodes.addAll(getNodes(process));
        }
      }

      final List<NodeCandidate> matchmaking;
      try {
        matchmaking = matchmakingEngine
            .matchmaking(task.requirements(job), reusableNodes, null, schedule.userId());
      } catch (MatchmakingException e) {
        throw new InstantiationException("Matchmaking failed.", e);
      }

      final List<ListenableFuture<Node>> allocate = resourcePool
          .allocate(schedule, matchmaking, reusableNodes, task.name());

      final Future<Collection<CloudiatorProcess>> collectionFuture = instantiationStrategySelector
          .get(schedule.instantiation())
          .deployTask(task, taskInterfaceSelection.get(task), schedule, allocate,
              dependencyGraph.forTask(task));
      futures.add(collectionFuture);

    }

    try {
      CloudiatorFutures.waitForFutures(futures);
    } catch (ExecutionException | InterruptedException e) {
      throw new InstantiationException("Instantiation failed.", e);
    } finally {
      cleanup(processToBeCleaned, nodesToBeCleaned);
    }

    return Objects
        .requireNonNull(findByIdAndUser(schedule.id(), schedule.userId()))
        .setState(
            ScheduleState.RUNNING);
  }

  private void cleanup(Set<CloudiatorProcess> processes, Set<Node> nodes) {

    try {
      deleteProcesses(processes).get();
      deleteNodes(nodes).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException("Unexpected exception during cleanup process");
    }
  }

  private ListenableFuture<?> deleteProcesses(Set<CloudiatorProcess> processes) {

    List<ListenableFuture<ProcessDeletedResponse>> futures = new LinkedList<>();

    for (CloudiatorProcess cloudiatorProcess : processes) {

      final DeleteProcessRequest deleteProcessRequest = DeleteProcessRequest.newBuilder()
          .setUserId(cloudiatorProcess.userId())
          .setProcessId(cloudiatorProcess.id()).build();

      final SettableFutureResponseCallback<ProcessDeletedResponse, ProcessDeletedResponse> future = SettableFutureResponseCallback.<ProcessDeletedResponse>create();

      processService.deleteProcessAsync(deleteProcessRequest, future);

      futures.add(future);
    }

    final ListenableFuture<List<ProcessDeletedResponse>> listListenableFuture = Futures
        .allAsList(futures);

    return listListenableFuture;

  }

  private ListenableFuture<?> deleteNodes(Set<Node> nodes) {

    List<ListenableFuture<NodeDeleteResponseMessage>> futures = new LinkedList<>();

    for (Node node : nodes) {

      final NodeDeleteMessage nodeDeleteMessage = NodeDeleteMessage.newBuilder()
          .setUserId(node.userId())
          .setNodeId(node.id()).build();

      final SettableFutureResponseCallback<NodeDeleteResponseMessage, NodeDeleteResponseMessage> future = SettableFutureResponseCallback.<NodeDeleteResponseMessage>create();

      nodeService.deleteNodeAsync(nodeDeleteMessage, future);

      futures.add(future);
    }

    final ListenableFuture<List<NodeDeleteResponseMessage>> listListenableFuture = Futures
        .allAsList(futures);

    return listListenableFuture;

  }

  private Set<CloudiatorProcess> processesPerTask(Schedule schedule, Task task) {

    return schedule.processes().stream().filter(
        cloudiatorProcess -> cloudiatorProcess.taskId().equals(task.name()))
        .collect(Collectors.toSet());
  }

  private Set<Node> getNodes(CloudiatorProcess cloudiatorProcess) {

    Set<Node> nodes = new HashSet<>(cloudiatorProcess.nodes().size());
    for (String nodeId : cloudiatorProcess.nodes()) {

      final Node node = nodeMessageRepository.getById(cloudiatorProcess.userId(), nodeId);

      checkState(node != null, String
          .format("Node %s referenced by process %s does not exist.", nodeId, cloudiatorProcess));

      nodes.add(node);

    }

    return nodes;
  }

}
