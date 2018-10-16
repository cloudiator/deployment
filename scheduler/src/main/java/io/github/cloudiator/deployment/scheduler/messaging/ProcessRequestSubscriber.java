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
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.messaging.ScheduleMessageRepository;
import io.github.cloudiator.deployment.scheduler.ProcessSpawner;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import java.util.Optional;
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
  private final ScheduleMessageRepository scheduleMessageRepository;
  private final ProcessSpawner processSpawner;
  private final NodeMessageRepository nodeMessageRepository;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private final ProcessDomainRepository processDomainRepository;

  @Inject
  public ProcessRequestSubscriber(ProcessService processService,
      MessageInterface messageInterface,
      JobMessageRepository jobMessageRepository,
      ScheduleMessageRepository scheduleMessageRepository,
      ProcessSpawner processSpawner,
      NodeMessageRepository nodeMessageRepository,
      ProcessDomainRepository processDomainRepository) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.jobMessageRepository = jobMessageRepository;
    this.scheduleMessageRepository = scheduleMessageRepository;
    this.processSpawner = processSpawner;
    this.nodeMessageRepository = nodeMessageRepository;
    this.processDomainRepository = processDomainRepository;
  }

  @Transactional
  void persistProcess(CloudiatorProcess cloudiatorProcess, String userId) {
    processDomainRepository.save(cloudiatorProcess, userId);
  }

  @Override
  public void run() {
    processService.subscribeCreateProcessRequest(new MessageCallback<CreateProcessRequest>() {
      @Override
      public void accept(String id, CreateProcessRequest content) {

        try {

          LOGGER.info(String.format("%s received new process request %s.", this, content));

          final String userId = content.getUserId();
          final String scheduleId = content.getProcess().getSchedule();
          final Schedule schedule = scheduleMessageRepository.getById(userId, scheduleId);

          if (schedule == null) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Schedule with the id %s does not exist", scheduleId))
                .build());
            return;
          }

          final String jobId = schedule.job();
          final String taskName = content.getProcess().getTask();
          final String nodeId = content.getProcess().getNode();

          final Node node = nodeMessageRepository.getById(userId, nodeId);

          if (node == null) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Node with the id %s does not exist", nodeId)).build());
            return;
          }

          final Job job = jobMessageRepository.getById(userId, jobId);

          if (job == null) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(500)
                .setMessage(String
                    .format("Job with the id %s does not exist but is referenced by schedule %s.",
                        jobId, schedule)).build());
            return;
          }

          final Optional<Task> optionalTask = job.getTask(taskName);

          if (!optionalTask.isPresent()) {
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String
                    .format("Task with name %s does not exist in job with id %s.", taskName, jobId))
                .build());
            return;
          }

          final Task task = optionalTask.get();

          LOGGER.info(String.format(
              "%s is spawning a new cloudiator process for user %s using processSpawner %s, schedule %s, job %s, task %s and node %s.",
              this, userId, processSpawner, schedule, job, task, node));

          //todo handle correctly type of task, currently we only assume lance
          final CloudiatorProcess cloudiatorProcess = processSpawner
              .spawn(userId, scheduleId, job, task, node);

          //persist the process
          persistProcess(cloudiatorProcess, userId);

          final ProcessCreatedResponse processCreatedResponse = ProcessCreatedResponse.newBuilder()
              .setProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess)).build();

          messageInterface.reply(id, processCreatedResponse);

        } catch (Exception e) {
          final String errorMessage = String
              .format("Unexpected error while processing request %s with id %s.", content, id);
          LOGGER.error(errorMessage);
          messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage(errorMessage)
              .build());
        }
      }

    });
  }
}
