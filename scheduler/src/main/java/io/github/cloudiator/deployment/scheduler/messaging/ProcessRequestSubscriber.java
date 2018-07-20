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
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.ProcessSpawner;
import java.util.Optional;
import java.util.function.Predicate;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProcessRequestSubscriber.class);
  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final JobMessageRepository jobMessageRepository;
  private final ProcessSpawner processSpawner;

  @Inject
  public ProcessRequestSubscriber(ProcessService processService,
      MessageInterface messageInterface,
      JobMessageRepository jobMessageRepository,
      ProcessSpawner processSpawner) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.jobMessageRepository = jobMessageRepository;
    this.processSpawner = processSpawner;
  }

  @Override
  public void run() {
    processService.subscribeCreateProcessRequest(new MessageCallback<CreateProcessRequest>() {
      @Override
      public void accept(String id, CreateProcessRequest content) {

        try {

          final String userId = content.getUserId();
          final String jobId = content.getProcess().getSchedule().getJob();

          final String taskName = content.getProcess().getTask();

          final Job job = jobMessageRepository.getById(userId, jobId);
          final Schedule schedule = new ScheduleImpl(content.getProcess().getSchedule().getId(),
              job);

          if (job == null) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Job with the id %s does not exist", jobId)).build());
            return;
          }

          final Optional<Task> optionalTask = job.tasks().stream().filter(new Predicate<Task>() {
            @Override
            public boolean test(Task task) {
              return task.name().equals(taskName);
            }
          }).collect(StreamUtil.getOnly());

          if (!optionalTask.isPresent()) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String
                    .format("Task with name %s does not exist in job with id %s.", taskName, jobId))
                .build());
            return;
          }

          //todo handle correctly type of task, currently we only assume lance
          processSpawner.spawn(userId, schedule, optionalTask.get());

          //todo, reply correctly

        } catch (Exception e) {
          LOGGER.error(String
              .format("Unexpected error while processing request %s with id %s.", content, id));
          //todo, reply with error
        }
      }

    });
  }
}
