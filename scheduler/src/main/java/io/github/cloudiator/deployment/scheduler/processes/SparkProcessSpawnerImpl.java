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

package io.github.cloudiator.deployment.scheduler.processes;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.messaging.SparkInterfaceConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.CreateSparkClusterRequest;
import org.cloudiator.messages.Process.CreateSparkProcessRequest;
import org.cloudiator.messages.Process.SparkClusterCreatedResponse;
import org.cloudiator.messages.Process.SparkProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.Nodes;
import org.cloudiator.messages.entities.ProcessEntities.SparkProcess;
import org.cloudiator.messages.entities.ProcessEntities.SparkProcess.Builder;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 07.11.2018.
 */
public class SparkProcessSpawnerImpl implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessSpawnerImpl.class);

  @Inject
  public SparkProcessSpawnerImpl(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    return taskInterface instanceof SparkInterface;
  }

  private Builder builder(String schedule, Job job, Task task,
      TaskInterface taskInterface) {

    return SparkProcess.newBuilder()
        .setSchedule(schedule)
        .setJob(JOB_CONVERTER.applyBack(job))
        .setTask(task.name())
        .setSparkInterface(SparkInterfaceConverter.INSTANCE.applyBack(
            (SparkInterface) taskInterface));

  }

  private CloudiatorProcess executeRequest(String userId, SparkProcess sparkProcess)
      throws ProcessSpawningException {

    final CreateSparkClusterRequest clusterRequest = CreateSparkClusterRequest.newBuilder()
        .setNodes(sparkProcess.getNodes()).setUserId(userId).build();
    try {

      SettableFutureResponseCallback<SparkClusterCreatedResponse, SparkClusterCreatedResponse> settableFutureResponseCallback = SettableFutureResponseCallback
          .create();

      processService
          .createSparkClusterAsync(clusterRequest, settableFutureResponseCallback);

      settableFutureResponseCallback.get();

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Error while deploying Spark cluster! " + e.getMessage());
      throw new ProcessSpawningException("Error while deploying Spark cluster! " + e.getMessage(),
          e);
    }

    final CreateSparkProcessRequest processRequest = CreateSparkProcessRequest.newBuilder()
        .setSpark(sparkProcess).setUserId(userId).build();

    SettableFutureResponseCallback<SparkProcessCreatedResponse, CloudiatorProcess> futureResponseCallback = SettableFutureResponseCallback
        .create(
            sparkProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(sparkProcessCreatedResponse.getProcess()));

    processService.createSparkProcessAsync(processRequest, futureResponseCallback);

    try {
      return futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          String.format("%s got interrupted while spawning process", this), e);
    } catch (ExecutionException e) {
      throw new ProcessSpawningException(e.getCause().getMessage(), e);
    }

  }

  @Override
  public CloudiatorSingleProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Node node) throws ProcessSpawningException {

    checkState(supports(taskInterface), String
        .format("TaskInterface of type %s is not supported by %s",
            taskInterface.getClass().getName(), this));

    return (CloudiatorSingleProcess) executeRequest(userId,
        builder(schedule.id(), job, task, taskInterface).setNode(NODE_CONVERTER.apply(node))
            .build());
  }

  @Override
  public CloudiatorClusterProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Set<Node> nodes) throws ProcessSpawningException {

    checkState(supports(taskInterface), String
        .format("TaskInterface of type %s is not supported by %s",
            taskInterface.getClass().getName(), this));

    return (CloudiatorClusterProcess) executeRequest(userId,
        builder(schedule.id(), job, task, taskInterface).setNodes(Nodes.newBuilder()
            .addAllNodes(nodes.stream().map(NODE_CONVERTER).collect(Collectors.toList())).build())
            .build());
  }
}
