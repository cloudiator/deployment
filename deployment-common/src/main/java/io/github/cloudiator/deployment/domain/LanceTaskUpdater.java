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

package io.github.cloudiator.deployment.domain;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.messaging.TaskConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.LanceUpdateRequest;
import org.cloudiator.messages.Process.LanceUpdateResponse;
import org.cloudiator.messages.entities.ProcessEntities.LanceUpdate;
import org.cloudiator.messages.entities.ProcessEntities.LanceUpdateType;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceTaskUpdater implements TaskUpdater {

  private final ProcessService processService;
  private static final TaskConverter TASK_CONVERTER = new TaskConverter();
  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceTaskUpdater.class);

  @Inject
  public LanceTaskUpdater(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    return taskInterface instanceof LanceInterface;
  }

  private void submit(String userId, LanceUpdate lanceUpdate) {
    final LanceUpdateRequest lanceUpdateRequest = LanceUpdateRequest.newBuilder()
        .setUserId(userId)
        .setLanceUpdate(lanceUpdate).build();

    SettableFutureResponseCallback<LanceUpdateResponse, LanceUpdateResponse> futureResponseCallback = SettableFutureResponseCallback
        .create();

    processService.updateLanceEnvironmentAsync(lanceUpdateRequest, futureResponseCallback);

    try {
      futureResponseCallback.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new IllegalStateException(e.getCause());
    }
  }

  @Override
  public void notifyNew(Schedule schedule, Job job, TaskInterface runningTaskInterface,
      Task runningTask, CloudiatorProcess newSpawned) {
    final Task spawnedTask = job.getTask(newSpawned.taskId())
        .orElseThrow(IllegalStateException::new);

    final LanceUpdate lanceUpdate = LanceUpdate.newBuilder().setScheduleId(schedule.id())
        .setJob(JobConverter.INSTANCE.applyBack(job))
        .setTaskSpawnedorDeleted(TASK_CONVERTER.applyBack(spawnedTask))
        .setTaskToBeUpdated(TASK_CONVERTER.applyBack(runningTask)).setProcessSpawnedorDeleted(
            ProcessMessageConverter.INSTANCE.applyBack(newSpawned)).setUpdateType(
            LanceUpdateType.INJECT).build();

    LOGGER.info(String.format(
        "Notify for new process for schedule %s, job %s, taskInterface %s, runningTask %s and newly spawned process %s.",
        schedule.id(), job.id(), runningTaskInterface, runningTask, newSpawned));

    submit(schedule.userId(), lanceUpdate);

  }

  @Override
  public void notifyDelete(Schedule schedule, Job job, TaskInterface toBeNotifiedTaskInterface,
      Task toBeNotifiedTask, CloudiatorProcess deleted) {

    final Task deletedTask = job.getTask(deleted.taskId())
        .orElseThrow(IllegalStateException::new);

    final LanceUpdate lanceUpdate = LanceUpdate.newBuilder().setScheduleId(schedule.id())
        .setJob(JobConverter.INSTANCE.applyBack(job))
        .setTaskSpawnedorDeleted(TASK_CONVERTER.applyBack(deletedTask))
        .setTaskToBeUpdated(TASK_CONVERTER.applyBack(toBeNotifiedTask)).setProcessSpawnedorDeleted(
            ProcessMessageConverter.INSTANCE.applyBack(deleted)).setUpdateType(
            LanceUpdateType.REMOVE).build();

    LOGGER.info(String.format(
        "Notify before deleting process for schedule %s, job %s, taskInterface %s, runningTask %s and deleted process %s.",
        schedule.id(), job.id(), toBeNotifiedTaskInterface, toBeNotifiedTask, deleted));

    submit(schedule.userId(), lanceUpdate);
  }
}
