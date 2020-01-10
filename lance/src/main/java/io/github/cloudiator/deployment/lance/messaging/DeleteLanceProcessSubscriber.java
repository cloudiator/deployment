/*
 * Copyright 2014-2019 University of Ulm
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

package io.github.cloudiator.deployment.lance.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.lance.DeleteLanceProcessStrategy;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.DeleteLanceProcessRequest;
import org.cloudiator.messages.Process.LanceProcessDeletedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLanceProcessSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteLanceProcessSubscriber.class);
  private final ProcessService processService;
  private final DeleteLanceProcessStrategy deleteLanceProcessStrategy;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private final MessageInterface messageInterface;

  @Inject
  public DeleteLanceProcessSubscriber(
      ProcessService processService,
      DeleteLanceProcessStrategy deleteLanceProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.deleteLanceProcessStrategy = deleteLanceProcessStrategy;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    processService.subscribeDeleteLanceProcessRequest(
        new MessageCallback<DeleteLanceProcessRequest>() {
          @Override
          public void accept(String id, DeleteLanceProcessRequest content) {

            LOGGER.info(String
                .format("Receiving request to delete process with id %s.", content.getProcessId()));

            try {
              deleteLanceProcessStrategy
                  .execute(content.getProcessId(), content.getScheduleId(), content.getTaskId(),
                      content.getJobId(),
                      NODE_MESSAGE_CONVERTER.applyBack(content.getNode()));

              messageInterface.reply(id, LanceProcessDeletedResponse.newBuilder().build());

            } catch (Exception e) {
              LOGGER.error(String
                  .format("Unexpected exception while killing lance process with id %s.",
                      e.getMessage()), e);
              messageInterface.reply(LanceProcessDeletedResponse.class, id,
                  Error.newBuilder().setCode(500).setMessage(
                      String
                          .format("Unexpected exception while killing lance process with id %s: %s",
                              content.getProcessId(), e.getMessage())).build());
            }
          }
        });
  }
}
