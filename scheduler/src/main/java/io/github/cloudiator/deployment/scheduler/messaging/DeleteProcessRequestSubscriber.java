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
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.scheduler.ProcessStateMachine;
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
  private final ProcessStateMachine processStateMachine;

  @Inject
  public DeleteProcessRequestSubscriber(
      ProcessService processService,
      ProcessDomainRepository processDomainRepository,
      MessageInterface messageInterface,
      ProcessStateMachine processStateMachine) {
    this.processService = processService;
    this.processDomainRepository = processDomainRepository;
    this.messageInterface = messageInterface;
    this.processStateMachine = processStateMachine;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  CloudiatorProcess getProcess(String processId, String userId) {
    return processDomainRepository.getByIdAndUser(processId, userId);
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

          if (!process.state().isRemovable()) {
            messageInterface.reply(ProcessDeletedResponse.class, id, Error.newBuilder().setCode(403)
                .setMessage(String
                    .format("Process with id %s is in state %s and can not be removed.", processId,
                        process.state()))
                .build());
            return;
          }

          processStateMachine.apply(process, ProcessState.DELETED, new Object[0]);

          LOGGER.info(String.format("Successfully deleted process %s.", process));

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
