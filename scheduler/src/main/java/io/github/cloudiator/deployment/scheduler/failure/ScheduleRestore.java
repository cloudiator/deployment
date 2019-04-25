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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import io.github.cloudiator.deployment.scheduler.exceptions.SchedulingException;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategy.CompositeWaitLock;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategy.WaitLock;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategySelector;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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

  @Inject
  public ScheduleRestore(ResourcePool resourcePool,
      JobMessageRepository jobMessageRepository,
      NodeMessageRepository nodeMessageRepository,
      InstantiationStrategySelector instantiationStrategySelector,
      ProcessService processService, NodeService nodeService,
      ScheduleDomainRepository scheduleDomainRepository) {
    this.resourcePool = resourcePool;
    this.jobMessageRepository = jobMessageRepository;
    this.nodeMessageRepository = nodeMessageRepository;
    this.instantiationStrategySelector = instantiationStrategySelector;
    this.processService = processService;
    this.nodeService = nodeService;
    this.scheduleDomainRepository = scheduleDomainRepository;
  }

  public Schedule heal(Schedule schedule) throws SchedulingException {

    final Job job = jobMessageRepository.getById(schedule.userId(), schedule.job());

    checkState(job != null, String
        .format("Job %s referenced by schedule %s does not longer exist", schedule.job(),
            schedule));

    Set<CloudiatorProcess> processToBeCleaned = new HashSet<>();
    Set<Node> nodesToBeCleaned = new HashSet<>();
    List<WaitLock> waitLocks = new LinkedList<>();

    for (Task task : job.tasks()) {

      Set<Node> reusableNodes = new LinkedHashSet<>();

      for (CloudiatorProcess process : processesPerTask(schedule, task)) {

        if (process.state().equals(ProcessState.ERROR)) {
          processToBeCleaned.add(process);
          nodesToBeCleaned.addAll(getNodes(process));
        } else {
          reusableNodes.addAll(getNodes(process));
        }
      }

      final List<ListenableFuture<Node>> allocate = resourcePool
          .allocate(schedule, task.requirements(job), reusableNodes, task.name());

      final WaitLock waitLock = instantiationStrategySelector.get(schedule.instantiation())
          .deployTask(task, schedule, allocate);
      waitLocks.add(waitLock);

    }

    new CompositeWaitLock(waitLocks).waitFor();

    cleanup(processToBeCleaned, nodesToBeCleaned);

    return Objects
        .requireNonNull(scheduleDomainRepository.findByIdAndUser(schedule.id(), schedule.userId()))
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
