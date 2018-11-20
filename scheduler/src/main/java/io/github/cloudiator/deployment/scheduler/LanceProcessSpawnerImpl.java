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

package io.github.cloudiator.deployment.scheduler;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.Future;
import org.cloudiator.messages.Process.CreateLanceProcessRequest;
import org.cloudiator.messages.Process.LanceProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.LanceProcess;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceProcessSpawnerImpl implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessSpawnerImpl.class);

  @Inject
  public LanceProcessSpawnerImpl(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(Task task) {
    try {
      task.interfaceOfType(LanceInterface.class);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER
          .debug("Provided task does not contain a LanceInterface! Skipping LanceProcessSpawner!");
      return false;
    }
  }

  @Override
  public Future<CloudiatorProcess> spawn(String userId, String schedule, Job job, Task task,
      Node node) {

    LOGGER.info(String
        .format("%s is spawning a new process for user: %s, Schedule %s, Task %s on Node %s", this,
            userId, schedule, task, node));

    final LanceProcess lanceProcess = LanceProcess.newBuilder()
        .setSchedule(schedule)
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_MESSAGE_CONVERTER.apply(node)).setTask(task.name()).build();
    final CreateLanceProcessRequest processRequest = CreateLanceProcessRequest.newBuilder()
        .setLance(lanceProcess).setUserId(userId).build();

    SettableFutureResponseCallback<LanceProcessCreatedResponse, CloudiatorProcess> futureResponseCallback = SettableFutureResponseCallback
        .create(
            lanceProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(lanceProcessCreatedResponse.getProcess()));

    processService.createLanceProcessAsync(processRequest, futureResponseCallback);

    return futureResponseCallback;

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
