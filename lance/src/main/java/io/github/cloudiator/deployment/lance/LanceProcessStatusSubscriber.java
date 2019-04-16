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

package io.github.cloudiator.deployment.lance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerStatus;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ProcessStatusQuery;
import org.cloudiator.messages.Process.ProcessStatusQuery.RunsOnCase;
import org.cloudiator.messages.Process.ProcessStatusResponse;
import org.cloudiator.messages.entities.ProcessEntities.ProcessState;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceProcessStatusSubscriber implements Runnable {

  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final LanceClientConnector lanceClientConnector;
  private static final Logger LOGGER = LoggerFactory.getLogger(LanceProcessStatusSubscriber.class);


  @Inject
  public LanceProcessStatusSubscriber(
      ProcessService processService, MessageInterface messageInterface,
      LanceClientConnector lanceClientConnector) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.lanceClientConnector = lanceClientConnector;
  }

  private static ProcessState determineState(ContainerStatus containerStatus) {

    switch (containerStatus) {
      case NEW:
      case CREATED:
      case CREATING:
      case BOOTSTRAPPED:
      case INITIALISING:
      case BOOTSTRAPPING:
        return ProcessState.PROCESS_STATE_PENDING;
      case READY:
      case SHUTTING_DOWN:
        return ProcessState.PROCESS_STATE_RUNNING;
      case DESTROYED:
      case CREATION_FAILED:
      case BOOTSTRAPPING_FAILED:
      case INITIALISATION_FAILED:
      case UNKNOWN:
        return ProcessState.PROCESS_STATE_ERROR;
      default:
        throw new AssertionError("Unknown container status " + containerStatus);
    }

  }

  @Override
  public void run() {

    messageInterface.subscribe(ProcessStatusQuery.class, ProcessStatusQuery.parser(),
        new MessageCallback<ProcessStatusQuery>() {
          @Override
          public void accept(String messageId, ProcessStatusQuery content) {

            CloudiatorProcess cloudiatorProcess = ProcessMessageConverter.INSTANCE
                .apply(content.getProcess());

            //check if this affects lance
            if (!cloudiatorProcess.type().equals(Type.LANCE)) {
              LOGGER.debug(String
                  .format("Ignoring process status request %s as it does not affect lance.",
                      content));
              return;
            }

            if (!(cloudiatorProcess instanceof CloudiatorSingleProcess) || !content.getRunsOnCase()
                .equals(
                    RunsOnCase.NODE) || !cloudiatorProcess.originId().isPresent()) {
              LOGGER.warn(String.format(
                  "Process status requests %s contains illegal lance process.",
                  content));
              messageInterface.reply(ProcessStatusResponse.class, messageId,
                  Error.newBuilder().setCode(500).setMessage("Illegal lance process provided.")
                      .build());
              return;
            }

            final Node apply = NodeToNodeMessageConverter.INSTANCE.applyBack(content.getNode());

            final String ip = apply.connectTo().ip();
            try {
              final LifecycleClient lifecycleClient = lanceClientConnector
                  .getLifecycleClient(ip, 5000, false);

              final ContainerStatus componentContainerStatus = lifecycleClient
                  .getComponentContainerStatus(
                      ComponentInstanceId.fromString(cloudiatorProcess.originId().get()), ip);

              messageInterface.reply(messageId, ProcessStatusResponse.newBuilder()
                  .setState(determineState(componentContainerStatus)).build());

            } catch (Exception e) {
              LOGGER.error(String.format(
                  "Could not retrieve status for lance process %s due to error %s. Replying with error status.",
                  cloudiatorProcess, e.getMessage()), e);
              messageInterface.reply(messageId,
                  ProcessStatusResponse.newBuilder().setState(ProcessState.PROCESS_STATE_ERROR)
                      .setInformation("Unable to connect to lance agent: " + e.getMessage())
                      .build());
            }


          }
        });


  }


}
