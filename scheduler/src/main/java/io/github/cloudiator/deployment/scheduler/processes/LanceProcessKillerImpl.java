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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.scheduler.exceptions.ProcessDeletionException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.DeleteLanceProcessRequest;
import org.cloudiator.messages.Process.LanceProcessDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceProcessKillerImpl implements ProcessKiller {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessKillerImpl.class);
  private final ProcessService processService;
  private final NodeMessageRepository nodeMessageRepository;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private final ScheduleDomainRepository scheduleDomainRepository;

  @Inject
  public LanceProcessKillerImpl(ProcessService processService,
      NodeMessageRepository nodeMessageRepository,
      ScheduleDomainRepository scheduleDomainRepository) {
    this.processService = processService;
    this.nodeMessageRepository = nodeMessageRepository;
    this.scheduleDomainRepository = scheduleDomainRepository;
  }

  @Override
  public boolean supports(CloudiatorProcess cloudiatorProcess) {
    return cloudiatorProcess.type().equals(Type.LANCE);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule findSchedule(CloudiatorProcess cloudiatorProcess) {
    return scheduleDomainRepository.findByProcess(cloudiatorProcess);
  }

  @Override
  public void kill(CloudiatorProcess cloudiatorProcess) throws ProcessDeletionException {

    final String userId = cloudiatorProcess.userId();

    LOGGER.info(String
        .format("%s is killing the process %s for user: %s", this,
            cloudiatorProcess, userId));

    if (cloudiatorProcess instanceof CloudiatorClusterProcess) {
      throw new IllegalStateException(
          String.format(
              "CloudiatorProcess %s is of type CloudiatorClusterProcess. This is not supported by LANCE.",
              cloudiatorProcess));
    }

    final Node byId = nodeMessageRepository
        .getById(userId, ((CloudiatorSingleProcess) cloudiatorProcess).node());

    if (byId == null) {
      throw new IllegalStateException(
          String.format("Could not find node for process %s.", cloudiatorProcess));
    }

    if (!cloudiatorProcess.originId().isPresent()) {
      throw new IllegalStateException(
          String.format("Can not delete process %s as originId is not set.", cloudiatorProcess));
    }

    final Schedule schedule = findSchedule(cloudiatorProcess);

    checkState(schedule != null,
        String.format("Could not retrieve schedule for process %s", cloudiatorProcess));

    final DeleteLanceProcessRequest deleteLanceProcessRequest = DeleteLanceProcessRequest
        .newBuilder()
        .setProcessId(cloudiatorProcess.originId().get())
        .setTaskId(cloudiatorProcess.taskId())
        .setJobId(schedule.job())
        .setScheduleId(schedule.id())
        .setUserId(userId)
        .setNode(NODE_MESSAGE_CONVERTER.apply(byId))
        .build();

    SettableFutureResponseCallback<LanceProcessDeletedResponse, LanceProcessDeletedResponse> futureResponseCallback = SettableFutureResponseCallback
        .create();

    processService.deleteLanceProcessAsync(deleteLanceProcessRequest, futureResponseCallback);

    try {
      futureResponseCallback.get();

      LOGGER.info(
          String
              .format("%s successfully killed the process %s for user %s.", this, cloudiatorProcess,
                  userId));
    } catch (InterruptedException e) {
      throw new IllegalStateException("Got interrupted while waiting for lance process deletion.");
    } catch (ExecutionException e) {
      throw new ProcessDeletionException("Error while deleting lance process.", e.getCause());
    }


  }
}
