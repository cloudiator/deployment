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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.FaasInterfaceConverter;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.CreateFaasProcessRequest;
import org.cloudiator.messages.Process.FaasProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.FaasProcess;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaasProcessSpawnerImpl implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(FaasProcessSpawnerImpl.class);

  @Inject
  public FaasProcessSpawnerImpl(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    return taskInterface instanceof FaasInterface;
  }

  @Override
  public CloudiatorSingleProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Node node) throws ProcessSpawningException {

    final FaasProcess faasProcess = FaasProcess.newBuilder()
        .setSchedule(schedule.id())
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_MESSAGE_CONVERTER.apply(node)).setTask(task.name())
        .setFaasInterface(FaasInterfaceConverter.INSTANCE.applyBack((FaasInterface) taskInterface))
        .build();
    CreateFaasProcessRequest processRequest = CreateFaasProcessRequest.newBuilder()
        .setFaas(faasProcess)
        .setUserId(userId).build();

    SettableFutureResponseCallback<FaasProcessCreatedResponse, CloudiatorProcess> futureResponseCallback =
        SettableFutureResponseCallback.create(
            faasProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(faasProcessCreatedResponse.getProcess()));

    processService.createFaasProcessAsync(processRequest, futureResponseCallback);

    try {
      return (CloudiatorSingleProcess) futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          String.format("%s got interrupted while spawning process.", this), e);
    } catch (ExecutionException e) {
      throw new ProcessSpawningException(e.getCause().getMessage(), e.getCause());
    }
  }

  @Override
  public CloudiatorClusterProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Set<Node> nodes) throws ProcessSpawningException {
    throw new UnsupportedOperationException(
        String.format("%s does not support running processes on clusters.", this));
  }


}
