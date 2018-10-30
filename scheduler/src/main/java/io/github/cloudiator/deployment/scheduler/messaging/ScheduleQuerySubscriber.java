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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.messaging.ScheduleConverter;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ScheduleQueryRequest;
import org.cloudiator.messages.Process.ScheduleQueryResponse;
import org.cloudiator.messages.Process.ScheduleQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleQuerySubscriber implements Runnable {

  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleQuerySubscriber.class);
  private static final ScheduleConverter SCHEDULE_CONVERTER = ScheduleConverter.INSTANCE;

  @Inject
  public ScheduleQuerySubscriber(
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessService processService, MessageInterface messageInterface) {
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processService = processService;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {

    processService.subscribeScheduleQueryRequest(new MessageCallback<ScheduleQueryRequest>() {
      @Override
      public void accept(String id, ScheduleQueryRequest content) {

        try {

          if (Strings.isNullOrEmpty(content.getUserId())) {
            throw new IllegalStateException("UserId in request is null or empty.");
          }

          final Builder builder = ScheduleQueryResponse.newBuilder();

          if (!Strings.isNullOrEmpty(content.getScheduleId())) {

            final Schedule byUserAndId = findByUserAndId(content.getUserId(), content.getScheduleId());
            if (byUserAndId != null) {
              builder.addSchedules(SCHEDULE_CONVERTER
                  .applyBack(byUserAndId));
            }
          } else {
            builder
                .addAllSchedules(
                    findByUser(content.getUserId()).stream().map(SCHEDULE_CONVERTER::applyBack)
                        .collect(
                            Collectors.toSet()));
          }

          messageInterface.reply(id, builder.build());


        } catch (Exception e) {

          LOGGER.error("Unexpected exception while querying schedules.", e);

          messageInterface.reply(ScheduleQueryResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage(String
                  .format("Unexpected exception during querying schedules: %s.", e.getMessage()))
              .build());
        }


      }
    });

  }

  @Transactional
  @Nullable
  Schedule findByUserAndId(String userId, String scheduleId) {
    return scheduleDomainRepository.findByIdAndUser(scheduleId, userId);
  }

  @Transactional
  Set<Schedule> findByUser(String userId) {
    return scheduleDomainRepository.findAllByUser(userId);
  }

}
