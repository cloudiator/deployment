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
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ProcessGroupMessageConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.ProcessSpawner;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageRepository;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.Optional;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

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
  private static final NodeGroupMessageToNodeGroup NODE_GROUP_MESSAGE_TO_NODE_GROUP = new NodeGroupMessageToNodeGroup();
  private static final ProcessGroupMessageConverter PROCESS_GROUP_MESSAGE_CONVERTER = new ProcessGroupMessageConverter();
  private final ProcessDomainRepository processDomainRepository;
  private final NodeGroupMessageRepository nodeGroupMessageRepository;

  @Inject
  public ProcessRequestSubscriber(ProcessService processService,
      MessageInterface messageInterface,
      JobMessageRepository jobMessageRepository,
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessSpawner processSpawner,
      NodeMessageRepository nodeMessageRepository,
      ProcessDomainRepository processDomainRepository,
      NodeGroupMessageRepository nodeGroupMessageRepository){
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.jobMessageRepository = jobMessageRepository;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processSpawner = processSpawner;
    this.nodeMessageRepository = nodeMessageRepository;
    this.processDomainRepository = processDomainRepository;
    this.nodeGroupMessageRepository = nodeGroupMessageRepository;
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
  void persistProcessGroup(ProcessGroup processGroup, String userId) {
    processDomainRepository.save(processGroup, userId);
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
          final String nodeGroupId = content.getProcess()
              .getNodeGroup();

          //not required anymore as nodeGroup is present
          /*
          LOGGER.debug(String.format("Retrieving node for process request %s.", id));
          final Node node = nodeMessageRepository.getById(userId, nodeId);

          if (node == null) {
            LOGGER.error(String.format("Node with the id %s does not exist.", nodeId));
            messageInterface.reply(ProcessCreatedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Node with the id %s does not exist", nodeId)).build());
            return;
          }
          LOGGER
              .debug(String.format("Found node %s for process request %s.", node.id(), id));
              */

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

          //get NodeGroup object from message repository
          final NodeGroup nodeGroup = nodeGroupMessageRepository
              .getById(userId, nodeGroupId);

          if(nodeGroup == null || nodeGroup.getNodes().isEmpty()){
            LOGGER.error(String
                .format("NodeGroup with the id %s on job with id %s is empty or does not contain any nodes!", nodeGroup,
                    jobId));
          }

          LOGGER.info(String.format(
              "%s is spawning a new cloudiator process for user %s using processSpawner %s, schedule %s, job %s, task %s and node %s.",
              this, userId, processSpawner, schedule, job, task, nodeGroupId));


          //todo handle correctly type of task, currently we only assume lance
          final ProcessGroup processGroup = processSpawner
              .spawn(userId, scheduleId, job, task, nodeGroup);


          //persist processes via process group
          persistProcessGroup(processGroup, userId);


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
