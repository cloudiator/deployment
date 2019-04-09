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
import de.uniulm.omi.cloudiator.util.CloudiatorFutures;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroupBuilder;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
  public boolean supports(Task task) {
    return task.interfaces().stream()
        .allMatch(iface -> iface instanceof FaasInterface);
  }

  @Override
  public ProcessGroup spawn(String userId, String schedule, Job job, Task task,
      Set<Node> nodes) {
    //Create all Lance processes of for the nodegroup
    List<Future<CloudiatorProcess>> futures = new ArrayList<>();

    for (Node node : nodes) {
      futures.add(spawn(userId, schedule, job, task, node));
    }

    //wait until all processes are spawned
    try {
      List<CloudiatorProcess> spawnedFaaSProcesses = null;
      try {
        spawnedFaaSProcesses = CloudiatorFutures.waitForFutures(futures);
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted while waiting for processes to spawn.", e);
        Thread.currentThread().interrupt();
      }

      return ProcessGroupBuilder.create().generateId().userId(userId).scheduleId(schedule)
          .addProcesses(spawnedFaaSProcesses).build();

    } catch (ExecutionException e) {
      LOGGER.error("Error while waiting for FaaSProcess to spawn!", e);
      throw new IllegalStateException(e);
    }
  }

  private Future<CloudiatorProcess> spawn(String userId, String schedule, Job job, Task task,
      Node node) {
    final FaasProcess faasProcess = FaasProcess.newBuilder()
        .setSchedule(schedule)
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_MESSAGE_CONVERTER.apply(node)).setTask(task.name()).build();
    CreateFaasProcessRequest processRequest = CreateFaasProcessRequest.newBuilder()
        .setFaas(faasProcess)
        .setUserId(userId).build();

    SettableFutureResponseCallback<FaasProcessCreatedResponse, CloudiatorProcess> futureResponseCallback =
        SettableFutureResponseCallback.create(
            faasProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(faasProcessCreatedResponse.getProcess()));

    processService.createFaasProcessAsync(processRequest, futureResponseCallback);

    return futureResponseCallback;
  }

}
