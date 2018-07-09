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
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateScheduleRequest;
import org.cloudiator.messages.Process.ScheduleCreatedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewScheduleSubscriber implements Runnable {

  private final ProcessService processService;
  private final ResourcePool resourcePool;
  private final JobMessageRepository jobMessageRepository;
  private final MessageInterface messageInterface;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NewScheduleSubscriber.class);

  @Inject
  public NewScheduleSubscriber(ProcessService processService,
      ResourcePool resourcePool,
      JobMessageRepository jobMessageRepository,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.resourcePool = resourcePool;
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

          for (Task task : job.tasks()) {
            resourcePool.allocate(userId, task.requirements());
          }
        } catch (Exception e) {
          LOGGER.error("Unexpected exception while handling schedule.", e);
        }

      }
    });
  }
}
