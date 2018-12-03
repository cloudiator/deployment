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
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroups;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
  private static final NodeToNodeMessageConverter NODE_TO_NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();
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
          .debug("Provided task does not contain a LanceInterface. Checking for DockerInterface...");
    }
    try {
      task.interfaceOfType(DockerInterface.class);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER
          .debug("Provided task does neither contain a LanceInterface nor DockerInterface! Skipping LanceProcessSpawner!");
      return false;
    }
  }

  @Override
  public ProcessGroup spawn(String userId, String schedule, Job job, Task task,
      NodeGroup nodeGroup) {

    //Create all Lance processes of for the nodegroup
    List<Future<CloudiatorProcess>> futures = new ArrayList<>();

    for(Node node : nodeGroup.getNodes()){
      futures.add(spawn(userId,schedule,job,task, node));
    }

    //wait until all processes are spawned
    try {
      List<CloudiatorProcess> spawnedLanceProcesses = waitForFutures(futures);

      ProcessGroup processGroup = ProcessGroups.of(spawnedLanceProcesses);


      return processGroup;


    } catch (ExecutionException e) {
      LOGGER.error("Error while waiting for LanceProcess to spawn!" ,e);
      throw  new IllegalStateException(e);
    }


  }


  private Future<CloudiatorProcess> spawn(String userId, String schedule, Job job, Task task,
      Node node) {


    LOGGER.info(String
        .format("%s is spawning a new process for user: %s, Schedule %s, Task %s on Node %s", this,
            userId, schedule, task, node));



    final LanceProcess lanceProcess = LanceProcess.newBuilder()
        .setSchedule(schedule)
        .setTask(task.name())
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_TO_NODE_MESSAGE_CONVERTER.apply(node)).build();
    final CreateLanceProcessRequest processRequest = CreateLanceProcessRequest.newBuilder()
        .setLance(lanceProcess).setUserId(userId).build();

    SettableFutureResponseCallback<LanceProcessCreatedResponse, CloudiatorProcess> futureResponseCallback = SettableFutureResponseCallback
        .create(
            lanceProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(lanceProcessCreatedResponse.getProcess()));

    processService.createLanceProcessAsync(processRequest, futureResponseCallback);

    return futureResponseCallback;

  }

  private List<CloudiatorProcess> waitForFutures(List<Future<CloudiatorProcess>> futures)
      throws ExecutionException {

    final int size = futures.size();
    List<CloudiatorProcess> results = new ArrayList<>(size);

    LOGGER.debug(String.format("Waiting for a total amount of %s processes", size));

    for (int i = 0; i < size; i++) {

      Future<CloudiatorProcess> future = futures.get(i);

      try {
        LOGGER.debug(String
            .format("Waiting for process %s of %s. Number of completed processes: %s", i, size,
                results.size()));
        final CloudiatorProcess cloudiatorProcess = future.get();
        results.add(cloudiatorProcess);
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted while waiting for  Lance processes to complete. Stopping.");
        Thread.currentThread().interrupt();
      }
    }

    return results;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
