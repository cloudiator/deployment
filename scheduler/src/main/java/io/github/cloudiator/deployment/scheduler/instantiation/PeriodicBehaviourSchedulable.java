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

package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CollisionHandling;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.graph.JobGraph;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.NodeService;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodicBehaviourSchedulable implements Schedulable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(PeriodicBehaviourSchedulable.class);

  private final AutomaticInstantiationStrategy automaticInstantiationStrategy;
  private final ResourcePool resourcePool;
  private final MatchmakingEngine matchmakingEngine;
  private final Job job;
  private final Task task;
  private final Schedule schedule;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ProcessService processService;
  private final NodeService nodeService;
  private final ExecutionService executionService;

  @Inject
  public PeriodicBehaviourSchedulable(AutomaticInstantiationStrategy automaticInstantiationStrategy,
      ResourcePool resourcePool,
      MatchmakingEngine matchmakingEngine,
      @Assisted Job job,
      @Assisted Task task,
      @Assisted Schedule schedule,
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessService processService, NodeService nodeService,
      @Assisted ExecutionService executionService) {
    this.automaticInstantiationStrategy = automaticInstantiationStrategy;
    this.resourcePool = resourcePool;
    this.matchmakingEngine = matchmakingEngine;
    this.job = job;
    this.task = task;
    this.schedule = schedule;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processService = processService;
    this.nodeService = nodeService;
    this.executionService = executionService;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule refreshSchedule() {
    return scheduleDomainRepository
        .findByIdAndUser(this.schedule.id(), this.schedule.userId());
  }

  @Override
  public long period() {
    return ((PeriodicBehaviour) task.behaviour()).interval().period();
  }

  @Override
  public long delay() {
    return 0;
  }

  @Override
  public TimeUnit timeUnit() {
    return ((PeriodicBehaviour) task.behaviour()).interval().timeUnit();
  }

  @Override
  public void run() {

    LOGGER.info(String.format("%s is starting a new execution.", this));

    if (((PeriodicBehaviour) task.behaviour()).collisionHandling()
        .equals(CollisionHandling.CANCEL)) {
      LOGGER.info("Submitting job to cancel previous executions as collision handling is "
          + CollisionHandling.CANCEL);
      this.executionService.execute(new CleanupPreviousExecutions());
    }

    if (!canExecute()) {
      LOGGER.warn(String.format("Skipping execution of %s.", this));
      return;
    }

    try {

      final TaskInterface taskInterface = new TaskInterfaceSelection().select(task);

      final Future<Collection<CloudiatorProcess>> collectionFuture = automaticInstantiationStrategy
          .deployTask(task, taskInterface, schedule, allocateResources(),
              DependencyGraph.noDependencies(task));

    } catch (Exception e) {
      LOGGER.error(String.format("Unexpected exception while running %s.", this), e);
    }
  }

  private List<NodeCandidate> performMatchmaking() throws MatchmakingException {
    return matchmakingEngine
        .matchmaking(task.requirements(job), Collections.emptyList(), null, schedule.userId());
  }

  private List<ListenableFuture<Node>> allocateResources() throws MatchmakingException {
    return resourcePool.allocate(refreshSchedule(), performMatchmaking(), task.name());
  }

  private boolean canExecute() {

    final JobGraph jobGraph = JobGraph.of(job);

    for (Task dependency : jobGraph.getDependencies(task, true)) {

      final Set<CloudiatorProcess> cloudiatorProcesses = refreshSchedule()
          .processesForTask(dependency);

      if (cloudiatorProcesses.isEmpty()) {
        LOGGER.warn(
            String.format(
                "Can not execute task %s of schedule %s as dependency %s has no running processes.",
                task, schedule, dependency));
        return false;
      }

      for (CloudiatorProcess cloudiatorProcess : cloudiatorProcesses) {
        if (!cloudiatorProcess.state().equals(ProcessState.RUNNING)) {
          LOGGER.warn(
              String.format(
                  "Can not execute task %s of schedule %s as dependency %s has processes that are currently in state transition.",
                  task, schedule, dependency));
          return false;
        }
      }


    }

    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("job", job)
        .add("task", task)
        .add("schedule", schedule)
        .toString();
  }

  private class CleanupPreviousExecutions implements Runnable {

    @Override
    public void run() {
      final Set<CloudiatorProcess> cloudiatorProcesses = refreshSchedule().processesForTask(task);

      for (CloudiatorProcess cloudiatorProcess : cloudiatorProcesses) {

        try {
          processService.deleteProcess(
              DeleteProcessRequest.newBuilder().setUserId(cloudiatorProcess.userId())
                  .setProcessId(cloudiatorProcess.id()).build());

          for (String node : cloudiatorProcess.nodes()) {

            nodeService.deleteNodeAsync(NodeDeleteMessage.newBuilder().build(),
                new ResponseCallback<NodeDeleteResponseMessage>() {
                  @Override
                  public void accept(@Nullable NodeDeleteResponseMessage nodeDeleteResponseMessage,
                      @Nullable Error error) {
                    if (error != null) {
                      LOGGER.warn(
                          String.format(
                              "Could not delete node %s. Could lead to illegal state, but is maybe deleted later",
                              node));
                    }
                  }
                });

          }

        } catch (ResponseException e) {
          LOGGER.warn(
              String.format(
                  "Could not delete previous executions of task %s. Trying again at the next iteration.",
                  task));
        }

      }


    }
  }
}
