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
import de.uniulm.omi.cloudiator.lance.application.ApplicationInstanceId;
import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClientRegistryWrapper;
import de.uniulm.omi.cloudiator.lance.container.standard.ExternalContextParameters;
import de.uniulm.omi.cloudiator.lance.container.standard.ExternalContextParameters.Builder;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerStatus;
import de.uniulm.omi.cloudiator.lance.lifecycle.LifecycleHandlerType;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.LanceUpdateRequest;
import org.cloudiator.messages.Process.LanceUpdateResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceUpdateSubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final LanceClientConnector lanceClientConnector;
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory.getLogger(LanceUpdateSubscriber.class);

  @Inject
  public LanceUpdateSubscriber(MessageInterface messageInterface,
      LanceClientConnector lanceClientConnector) {
    this.messageInterface = messageInterface;
    this.lanceClientConnector = lanceClientConnector;
  }


  @Override
  public void run() {
    messageInterface.subscribe(LanceUpdateRequest.class, LanceUpdateRequest.parser(),
        new MessageCallback<LanceUpdateRequest>() {
          @Override
          public void accept(String id, LanceUpdateRequest content) {

            try {

              LOGGER.debug(String.format("Receiving lance update request %s.", content));

              CloudiatorProcess spawnedProcess = PROCESS_MESSAGE_CONVERTER
                  .apply(content.getLanceUpdate().getProcessSpawned());

              Job job = JOB_CONVERTER.apply(content.getLanceUpdate().getJob());

              final LifecycleClientRegistryWrapper registryWrapper = LifecycleClientRegistryWrapper
                  .getInstance();

              final ApplicationInstanceId applicationInstanceId = ApplicationInstanceId
                  .fromString(content.getLanceUpdate().getScheduleId());

              final ComponentId componentId = ComponentIdGenerator
                  .generate(job.id(), content.getLanceUpdate().getTaskSpawned().getName());

              final ComponentInstanceId componentInstanceId = ComponentInstanceId
                  .fromString(spawnedProcess.id());

              final ExternalContextParameters externalContextParameters = new Builder()
                  .appId(applicationInstanceId)
                  .compId(componentId).contStatus(
                      ContainerStatus.READY).compInstId(componentInstanceId).compInstType(
                      LifecycleHandlerType.START).taskName(spawnedProcess.taskId())
                  .pubIp(spawnedProcess.endpoint().get()).providedPortContext(
                      generateProvidedPortContext(job, spawnedProcess,
                          content.getLanceUpdate().getTaskToBeUpdated().getName()))
                  .build();

              LOGGER.info(String
                  .format("Updating for job %s. Task that spawned is %s, task to be updated is %s",
                      job.id(), spawnedProcess.taskId(),
                      content.getLanceUpdate().getTaskToBeUpdated().getName()));

              LOGGER.debug(String
                  .format("Injecting external context parameters %s", externalContextParameters));

              registryWrapper.injectExternalDeploymentContext(externalContextParameters);

              messageInterface.reply(id, LanceUpdateResponse.newBuilder().build());

            } catch (Exception e) {
              LOGGER.error("Error while updating lance config", e);
              messageInterface.reply(id,
                  Error.newBuilder().setMessage("Error occurred " + e.getMessage()).setCode(500)
                      .build());
            }


          }
        });
  }

  private static final ExternalContextParameters.ProvidedPortContext generateProvidedPortContext(
      Job job, CloudiatorProcess running, String toBeUpdated) {

    Task runningTask = job.getTask(running.taskId()).orElseThrow(IllegalStateException::new);
    Task toBeUpdatedTask = job.getTask(toBeUpdated)
        .orElseThrow(IllegalStateException::new);

    Communication communication = job.between(runningTask, toBeUpdatedTask)
        .orElseThrow(IllegalStateException::new);

    final int port = job.providedPort(communication).port();

    return new ExternalContextParameters.ProvidedPortContext(communication.portProvided(), port);
  }
}
