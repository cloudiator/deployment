/*
 * Copyright 2014-2018 University of Ulm
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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.scheduler.instantiation.ScheduleDeletionStrategy;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.DeleteScheduleRequest;
import org.cloudiator.messages.Process.ScheduleDeleteResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteScheduleRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteScheduleRequestSubscriber.class);

  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ScheduleDeletionStrategy scheduleDeletionStrategy;


  @Inject
  public DeleteScheduleRequestSubscriber(
      ProcessService processService, MessageInterface messageInterface,
      ScheduleDomainRepository scheduleDomainRepository,
      ScheduleDeletionStrategy scheduleDeletionStrategy) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.scheduleDeletionStrategy = scheduleDeletionStrategy;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void deleteSchedule(Schedule schedule, String userId) {
    scheduleDomainRepository.delete(schedule, userId);
  }

  @Override
  public void run() {

    processService.subscribeScheduleDeleteRequest(new MessageCallback<DeleteScheduleRequest>() {
      @Override
      public void accept(String id, DeleteScheduleRequest content) {

        final String userId = content.getUserId();
        final String scheduleId = content.getScheduleId();

        try {

          final Schedule schedule = scheduleDomainRepository.findByIdAndUser(scheduleId, userId);

          if (schedule == null) {
            messageInterface.reply(ScheduleDeleteResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Schedule with the id %s does not exist.", scheduleId))
                .build());
          }

          scheduleDeletionStrategy.delete(schedule, userId);

          deleteSchedule(schedule, userId);

          messageInterface.reply(id, ScheduleDeleteResponse.newBuilder().build());

        } catch (Exception e) {
          LOGGER.error("Unexpected exception while deleting schedule " + scheduleId, e);
          messageInterface.reply(ScheduleDeleteResponse.class, id,
              Error.newBuilder().setCode(500).setMessage(String
                  .format("Unexpected error while deleting schedule %s: %s", scheduleId,
                      e.getMessage())).build());
        }
      }

    });

  }
}
