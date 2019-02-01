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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.messaging.InstantiationConverter;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ScheduleConverter;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategy;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateScheduleRequest;
import org.cloudiator.messages.Process.ScheduleCreatedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleRequestSubscriber implements Runnable {

  private final ProcessService processService;
  private final JobMessageRepository jobMessageRepository;
  private final MessageInterface messageInterface;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleRequestSubscriber.class);

  private static final ScheduleConverter SCHEDULE_CONVERTER = ScheduleConverter.INSTANCE;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final InstantiationStrategy instantiationStrategy;


  @Inject
  public ScheduleRequestSubscriber(ProcessService processService,
      JobMessageRepository jobMessageRepository,
      MessageInterface messageInterface,
      ScheduleDomainRepository scheduleDomainRepository,
      InstantiationStrategy instantiationStrategy) {
    this.processService = processService;
    this.jobMessageRepository = jobMessageRepository;
    this.messageInterface = messageInterface;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.instantiationStrategy = instantiationStrategy;
  }

  @Transactional
  void persistSchedule(Schedule schedule) {
    scheduleDomainRepository.save(schedule);
  }

  @Override
  public void run() {
    processService.subscribeSchedule(new MessageCallback<CreateScheduleRequest>() {
      @Override
      public void accept(String id, CreateScheduleRequest content) {

        try {

          final String jobId = content.getSchedule().getJob();

          final Instantiation instantiation = InstantiationConverter.INSTANCE
              .apply(content.getSchedule().getInstantiation());
          final String userId = content.getUserId();

          LOGGER.info(String.format(
              "%s is receiving a new schedule request for job %s by user %s. Instantiation will be %s",
              this, jobId, userId, instantiation));

          //retrieve the job
          Job job = jobMessageRepository.getById(userId, jobId);
          if (job == null) {
            messageInterface.reply(ScheduleCreatedResponse.class, id,
                Error.newBuilder().setCode(404).setMessage("Could not find job with id " + jobId)
                    .build());
            return;
          }

          Schedule schedule = ScheduleImpl.create(job, instantiation);

          //persist the schedule
          persistSchedule(schedule);

          LOGGER.info(String
              .format("Using strategy %s to instantiate schedule %s, job %s for user %s",
                  instantiationStrategy, schedule, job, userId));

          //start the instantiation
          instantiationStrategy.instantiate(schedule, job, userId);

          //refresh the schedule object to receive the created processes
          Schedule createdSchedule = scheduleDomainRepository
              .findByIdAndUser(schedule.id(), userId);

          checkState(createdSchedule != null, String
              .format("Expected schedule with id %s to exist, but received null", schedule.id()));

          messageInterface.reply(id,
              ScheduleCreatedResponse.newBuilder()
                  .setSchedule(SCHEDULE_CONVERTER.applyBack(createdSchedule))
                  .build());

        } catch (Exception e) {
          LOGGER.error("Unexpected exception while processing schedule request.", e);
          messageInterface.reply(ScheduleCreatedResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage(
                  String.format("Unexpected exception while processing schedule request: %s",
                      e.getMessage()))
              .build());
        }


      }
    });
  }
}
