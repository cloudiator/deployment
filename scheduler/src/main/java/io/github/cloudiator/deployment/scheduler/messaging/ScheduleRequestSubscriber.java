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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.CreateScheduleRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.Process.ScheduleCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.ProcessNew;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleRequestSubscriber implements Runnable {

  private final ProcessService processService;
  private final JobMessageRepository jobMessageRepository;
  private final MessageInterface messageInterface;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleRequestSubscriber.class);

  @Inject
  public ScheduleRequestSubscriber(ProcessService processService,
      JobMessageRepository jobMessageRepository,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.jobMessageRepository = jobMessageRepository;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    processService.subscribeSchedule(new MessageCallback<CreateScheduleRequest>() {
      @Override
      public void accept(String id, CreateScheduleRequest content) {

        try {

          final String jobId = content.getSchedule().getJob();
          final String userId = content.getUserId();

          //retrieve the job
          Job job = jobMessageRepository.getById(userId, jobId);
          if (job == null) {
            messageInterface.reply(ScheduleCreatedResponse.class, id,
                Error.newBuilder().setCode(404).setMessage("Could not find job with id " + jobId)
                    .build());
            return;
          }

          Schedule schedule = ScheduleImpl.of(job);

          //process request for each task
          for (Task task : job.tasks()) {

            final ProcessNew newProcess = ProcessNew.newBuilder().setSchedule(
                ProcessEntities.Schedule.newBuilder().setId(schedule.id())
                    .setJob(schedule.job().id()).build()).setTask(task.name())
                .build();
            final CreateProcessRequest createProcessRequest = CreateProcessRequest.newBuilder()
                .setUserId(userId).setProcess(newProcess).build();

            //todo: forward the response
            processService.createProcessAsync(createProcessRequest,
                new ResponseCallback<ProcessCreatedResponse>() {
                  @Override
                  public void accept(@Nullable ProcessCreatedResponse content,
                      @Nullable Error error) {

                  }
                });

          }
        } catch (Exception e) {
          //todo: reply with error
          LOGGER.error("Unexpected exception while handling schedule.", e);
        }

      }
    });
  }
}
