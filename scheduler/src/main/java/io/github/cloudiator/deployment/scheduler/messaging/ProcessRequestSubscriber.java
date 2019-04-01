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
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ProcessGroupMessageConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.processes.ProcessSpawner;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ProcessSpawner processSpawner;
  private final NodeMessageRepository nodeMessageRepository;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final ProcessGroupMessageConverter PROCESS_GROUP_MESSAGE_CONVERTER = new ProcessGroupMessageConverter();
  private final ProcessDomainRepository processDomainRepository;

  @Inject
  public ProcessRequestSubscriber(ProcessService processService,
      MessageInterface messageInterface,
      JobMessageRepository jobMessageRepository,
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessSpawner processSpawner,
      NodeMessageRepository nodeMessageRepository,
      ProcessDomainRepository processDomainRepository) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.jobMessageRepository = jobMessageRepository;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processSpawner = processSpawner;
    this.nodeMessageRepository = nodeMessageRepository;
    this.processDomainRepository = processDomainRepository;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule retrieveSchedule(String scheduleId, String userId) {
    return scheduleDomainRepository.findByIdAndUser(scheduleId, userId);
  }

  /*
  @SuppressWarnings("WeakerAccess")
  @Transactional
  void persistProcess(CloudiatorProcess cloudiatorProcess, String userId) {
    processDomainRepository.save(cloudiatorProcess, userId);
  }
  */

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void persistProcessGroup(ProcessGroup processGroup) {
    processDomainRepository.save(processGroup);
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

          LOGGER.debug(String.format("Retrieving schedule for process request %s.", id));
          final Schedule schedule = retrieveSchedule(scheduleId, userId);

          if (schedule == null) {
            LOGGER.error(String.format("Schedule with the id %s does not exist.", scheduleId));
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Schedule with the id %s does not exist", scheduleId))
                .build());
            return;
          }
          LOGGER
              .debug(String.format("Found schedule %s for process request %s.", schedule.id(), id));

          final String jobId = schedule.job();
          final String taskName = content.getProcess().getTask();
          final Set<String> nodeIds = new HashSet<>(content.getProcess().getNodesList());

          //retrieve the nodes
          final Set<Node> nodes = new HashSet<>();
          for (String nodeId : nodeIds) {
            final Node node = nodeMessageRepository.getById(userId, id);
            if (node == null) {
              messageInterface.reply(ProcessCreatedResponse.class, id,
                  Error.newBuilder().setCode(404)
                      .setMessage(String.format("Node with the id %s does not exist.", nodeId))
                      .build());
              return;
            }
            if (!node.state().equals(NodeState.RUNNING)) {
              messageInterface.reply(ProcessCreatedResponse.class, id,
                  Error.newBuilder().setCode(400)
                      .setMessage(String
                          .format("Node %s with the id %s is in illegal state %s.", node, nodeId,
                              node.state()))
                      .build());
              return;
            }
            nodes.add(node);
          }

          LOGGER.debug(String.format("Retrieving job for process request %s.", id));
          final Job job = jobMessageRepository.getById(userId, jobId);

          if (job == null) {
            LOGGER.error(String.format("Job with the id %s does not exist.", jobId));
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(500)
                .setMessage(String
                    .format("Job with the id %s does not exist but is referenced by schedule %s.",
                        jobId, schedule)).build());
            return;
          }
          LOGGER
              .debug(String.format("Found job %s for process request %s.", job.id(), id));

          LOGGER.debug(String.format("Checking task for process request %s.", id));
          final Optional<Task> optionalTask = job.getTask(taskName);

          if (!optionalTask.isPresent()) {
            LOGGER.error(String
                .format("Task with the name %s on job with id %s does not exist.", taskName,
                    jobId));
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String
                    .format("Task with name %s does not exist in job with id %s.", taskName, jobId))
                .build());
            return;
          }

          final Task task = optionalTask.get();
          LOGGER
              .debug(String.format("Found task %s for process request %s.", task, id));

          LOGGER.info(String.format(
              "%s is spawning a new cloudiator process for user %s using processSpawner %s, schedule %s, job %s, task %s and nodes %s.",
              this, userId, processSpawner, schedule, job, task, nodeIds));

          //convert nodes

          final ProcessGroup processGroup = processSpawner
              .spawn(userId, scheduleId, job, task, nodes);

          //persist processes via process group
          persistProcessGroup(processGroup);

          final ProcessCreatedResponse processCreatedResponse = ProcessCreatedResponse.newBuilder()
              .setProcessGroup(PROCESS_GROUP_MESSAGE_CONVERTER.applyBack(processGroup))
              .build();

          messageInterface.reply(id, processCreatedResponse);

        } catch (Exception e) {
          final String errorMessage = String
              .format("Unexpected error while processing request %s with id %s.", content, id);
          LOGGER.error(errorMessage, e);
          messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage(errorMessage)
              .build());
        }
      }

    });
  }
}
