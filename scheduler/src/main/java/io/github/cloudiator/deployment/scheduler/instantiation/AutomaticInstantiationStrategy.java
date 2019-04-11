/*
 * Copyright 2018 University of Ulm
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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import io.github.cloudiator.deployment.scheduler.exceptions.SchedulingException;
import io.github.cloudiator.domain.Node;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.NodeCluster;
import org.cloudiator.messages.entities.ProcessEntities.ProcessNew;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticInstantiationStrategy implements InstantiationStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AutomaticInstantiationStrategy.class);
  private static final ExecutorService EXECUTOR = new LoggingThreadPoolExecutor(0,
      2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  private final ResourcePool resourcePool;
  private final ProcessService processService;

  @Inject
  public AutomaticInstantiationStrategy(
      ResourcePool resourcePool, ProcessService processService) {
    this.resourcePool = resourcePool;
    this.processService = processService;
  }


  @Override
  public boolean supports(Instantiation instantiation) {
    return instantiation.equals(Instantiation.AUTOMATIC);
  }

  private ListenableFuture<CloudiatorProcess> submitProcess(Schedule schedule,
      ProcessNew processNew) {
    final CreateProcessRequest createProcessRequest = CreateProcessRequest
        .newBuilder()
        .setUserId(schedule.userId()).setProcess(processNew).build();

    final SettableFutureResponseCallback<ProcessCreatedResponse, CloudiatorProcess> processRequestFuture = SettableFutureResponseCallback
        .create(processCreatedResponse -> ProcessMessageConverter.INSTANCE
            .apply(processCreatedResponse.getProcess()));

    processService.createProcessAsync(createProcessRequest, processRequestFuture);

    return processRequestFuture;
  }

  private class NodeCallback implements FutureCallback<Node> {

    private final Schedule schedule;
    private final Task task;
    private final TaskInterface taskInterface;

    private NodeCallback(Schedule schedule, Task task,
        TaskInterface taskInterface) {
      this.schedule = schedule;
      this.task = task;
      this.taskInterface = taskInterface;
    }

    @Override
    public void onSuccess(Node node) {

      LOGGER.info(String.format("Node %s was created. Starting processes.",
          node));

      final ProcessNew build = ProcessNew.newBuilder().setSchedule(
          schedule.id()).setTask(task.name())
          .setTaskInterface(taskInterface.getClass().getCanonicalName())
          .setNode(node.id()).build();

      submitProcess(schedule, build);

    }

    @Override
    public void onFailure(Throwable t) {
      throw new IllegalStateException("Unexpected exception while spawning node", t);
    }

  }

  private class ClusterCallback implements FutureCallback<List<Node>> {

    private final Schedule schedule;
    private final Task task;
    private final TaskInterface taskInterface;

    private ClusterCallback(Schedule schedule, Task task,
        TaskInterface taskInterface) {
      this.schedule = schedule;
      this.task = task;
      this.taskInterface = taskInterface;
    }

    @Override
    public void onSuccess(List<Node> nodes) {

      LOGGER.info(String.format("Nodes %s were created. Starting processes.",
          nodes));

      final ProcessNew build = ProcessNew.newBuilder().setSchedule(
          schedule.id()).setTask(task.name())
          .setTaskInterface(taskInterface.getClass().getCanonicalName())
          .setCluster(NodeCluster.newBuilder()
              .addAllNodes(nodes.stream().map(Identifiable::id).collect(Collectors.toList()))
              .build()).build();

      submitProcess(schedule, build);

    }

    @Override
    public void onFailure(Throwable t) {
      throw new IllegalStateException("Unexpected exception while spawning node", t);
    }

  }

  @Override
  public void instantiate(Schedule schedule, Job job, String userId) throws InstantiationException {

    checkState(supports(schedule.instantiation()),
        String.format("%s does not support instantiation %s.", this, schedule.instantiation()));

    //for each task
    for (Iterator<Task> iter = job.tasksInOrder(); iter.hasNext(); ) {
      final Task task = iter.next();

      LOGGER.info(String.format("Allocating the resources for task %s of job %s", task, job));

      try {
        final List<ListenableFuture<Node>> allocate = resourcePool
            .allocate(schedule, task.requirements(job), task.name());

        //select the interface
        final TaskInterface taskInterface = new TaskInterfaceSelectionPlaceholder()
            .select(task);

        switch (taskInterface.processMapping()) {
          case CLUSTER:

            final ListenableFuture<List<Node>> nodeFutures = Futures.allAsList(allocate);
            Futures.addCallback(nodeFutures, new ClusterCallback(schedule, task, taskInterface),
                EXECUTOR);
            break;
          case SINGLE:
            for (ListenableFuture<Node> nodeFuture : allocate) {
              Futures.addCallback(nodeFuture, new NodeCallback(schedule, task, taskInterface),
                  EXECUTOR);
            }
            break;
          default:
            throw new AssertionError("Unknown process mapping " + taskInterface.processMapping());
        }

      } catch (SchedulingException e) {
        throw new InstantiationException("Error while scheduling.", e);
      }

    }
  }

}
