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
import io.github.cloudiator.deployment.scheduler.processes.ProcessKiller;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messages.Process.ProcessDeletedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteProcessRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteProcessRequestSubscriber.class);
  private final ProcessService processService;
  private final ProcessDomainRepository processDomainRepository;
  private final MessageInterface messageInterface;
  private final ProcessKiller processKiller;

  @Inject
  public DeleteProcessRequestSubscriber(
      ProcessService processService,
      ProcessDomainRepository processDomainRepository,
      MessageInterface messageInterface,
      ProcessKiller processKiller) {
    this.processService = processService;
    this.processDomainRepository = processDomainRepository;
    this.messageInterface = messageInterface;
    this.processKiller = processKiller;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  CloudiatorProcess getProcess(String processId, String userId) {
    return processDomainRepository.getByIdAndUser(processId, userId);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void deleteProcess(CloudiatorProcess cloudiatorProcess, String userId) {
    processDomainRepository.delete(cloudiatorProcess.id(), userId);
  }

  @Override
  public void run() {
    processService.subscribeDeleteProcessRequest(new MessageCallback<DeleteProcessRequest>() {
      @Override
      public void accept(String id, DeleteProcessRequest content) {

        final String userId = content.getUserId();
        final String processId = content.getProcessId();

        LOGGER.info(String
            .format("Getting request to delete process %s for user %s: %s", processId, userId,
                content));

        try {

          //retrieve the process
          final CloudiatorProcess process = getProcess(processId, userId);

          if (!process.state().isRemovable()) {
            messageInterface.reply(ProcessDeletedResponse.class, id, Error.newBuilder().setCode(403)
                .setMessage(String
                    .format("Process with id %s is in state %s and can not be removed.", processId,
                        process.state()))
                .build());
            return;
          }

          if (process == null) {
            messageInterface.reply(ProcessDeletedResponse.class, id, Error.newBuilder().setCode(404)
                .setMessage(String.format("Process with id %s does not exist.", processId))
                .build());
            return;
          }

          if (!processKiller.supports(process)) {
            messageInterface.reply(ProcessDeletedResponse.class, id, Error.newBuilder().setCode(504)
                .setMessage(String.format("Deleting process of type %s is currently not supported.",
                    process.type())).build());
            return;
          }

          processKiller.kill(process);

          deleteProcess(process, userId);

          LOGGER.info(String.format("Successfully delete process %s.", process));

          messageInterface.reply(id, ProcessDeletedResponse.newBuilder().build());

        } catch (Exception e) {
          LOGGER.error(String
              .format("Unexpected exception while deleting process with id %s", processId), e);
          messageInterface.reply(ProcessDeletedResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage(
                  String.format("Unexpected exception while deleting process with id %s: %s",
                      processId, e.getMessage())).build());
        }


      }
    });
  }
}
