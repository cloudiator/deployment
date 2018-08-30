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

package io.github.cloudiator.deployment.lance;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.LanceProcessCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLanceProcessSubscriber implements Runnable {

  private final ProcessService processService;
  private final NodeToNodeMessageConverter nodeMessageToNodeConverter = new NodeToNodeMessageConverter();
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private final CreateLanceProcessStrategy createLanceProcessStrategy;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLanceProcessSubscriber.class);
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private final MessageInterface messageInterface;

  @Inject
  public CreateLanceProcessSubscriber(
      ProcessService processService,
      CreateLanceProcessStrategy createLanceProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.createLanceProcessStrategy = createLanceProcessStrategy;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    processService.subscribeCreateLanceProcessRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();
            final Job job = JOB_CONVERTER.apply(content.getLance().getJob());
            final String task = content.getLance().getTask();
            final Node node = nodeMessageToNodeConverter.applyBack(content.getLance().getNode());
            final String schedule = content.getLance().getSchedule();

            final CloudiatorProcess cloudiatorProcess = createLanceProcessStrategy
                .execute(userId, schedule, job, job.getTask(task).orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Job %s does not contain task %s", job, task))), node);

            final LanceProcessCreatedResponse lanceProcessCreatedResponse = LanceProcessCreatedResponse
                .newBuilder()
                .setProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess)).build();

            messageInterface.reply(id, lanceProcessCreatedResponse);

          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(LanceProcessCreatedResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });
  }
}
