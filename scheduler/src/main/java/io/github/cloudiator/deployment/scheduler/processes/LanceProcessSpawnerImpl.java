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
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.DockerInterfaceConverter;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.LanceInterfaceConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.CreateLanceProcessRequest;
import org.cloudiator.messages.Process.LanceProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.LanceProcess;
import org.cloudiator.messages.entities.ProcessEntities.LanceProcess.Builder;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceProcessSpawnerImpl implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_TO_NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessSpawnerImpl.class);

  @Inject
  public LanceProcessSpawnerImpl(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    return taskInterface instanceof LanceInterface || taskInterface instanceof DockerInterface;
  }

  @Override
  public CloudiatorClusterProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Set<Node> nodes) {
    throw new UnsupportedOperationException(
        String.format("%s does not support running processes on clusters.", this));
  }


  @Override
  public CloudiatorSingleProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface,
      Node node) throws ProcessSpawningException {

    checkState(supports(taskInterface),
        String.format("%s does not support task interface %s.", this, taskInterface));

    LOGGER.info(String
        .format("%s is spawning a new process for user: %s, Schedule %s, Task %s on Node %s", this,
            userId, schedule, task, node));

    final Builder builder = LanceProcess.newBuilder()
        .setSchedule(schedule.id())
        .setTask(task.name())
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_TO_NODE_MESSAGE_CONVERTER.apply(node));

    if (taskInterface instanceof LanceInterface) {
      builder.setLanceInterface(LanceInterfaceConverter.INSTANCE.applyBack(
          (LanceInterface) taskInterface));
    } else if (taskInterface instanceof DockerInterface) {
      builder.setDockerInterface(DockerInterfaceConverter.INSTANCE.applyBack(
          (DockerInterface) taskInterface));
    } else {
      throw new AssertionError(String
          .format("Illegal type %s of task interface not supported by %s.",
              taskInterface.getClass().getName(), this));
    }

    final CreateLanceProcessRequest processRequest = CreateLanceProcessRequest.newBuilder()
        .setLance(builder.build()).setUserId(userId).build();

    SettableFutureResponseCallback<LanceProcessCreatedResponse, CloudiatorSingleProcess> futureResponseCallback = SettableFutureResponseCallback
        .create(
            lanceProcessCreatedResponse -> (CloudiatorSingleProcess) PROCESS_MESSAGE_CONVERTER
                .apply(lanceProcessCreatedResponse.getProcess()));

    processService.createLanceProcessAsync(processRequest, futureResponseCallback);

    try {
      return futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Execution of process spawning got interrupted.");
    } catch (ExecutionException e) {
      throw new ProcessSpawningException(e.getCause().getMessage(), e);
    }

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }


}
