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
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.InstallationEntities.Installation;
import org.cloudiator.messages.InstallationEntities.Installation.Builder;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.InstallationRequestService;

public class LanceInstallationStrategy {

  private final InstallationRequestService installationRequestService;
  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();

  @Inject
  public LanceInstallationStrategy(
      InstallationRequestService installationRequestService) {
    this.installationRequestService = installationRequestService;
  }

  void execute(String userId, Node node, ContainerType containerType) {

    final Builder builder = Installation.newBuilder()
        .setNode(nodeToNodeMessageConverter.apply(node))
        .addTool(Tool.LANCE);
    //Needed for Lifecycle DockerContainers
    if (ContainerType.DOCKER.equals(containerType) || ContainerType.DOCKER_REMOTE
        .equals(containerType)) {
      builder.addTool(Tool.DOCKER);
    }
    doExecute(builder, userId);
  }

  //DockerInterface doesn't need a containerType
  void execute(String userId, Node node) {

    final Builder builder = Installation.newBuilder()
        .setNode(nodeToNodeMessageConverter.apply(node))
        .addTool(Tool.LANCE)
        .addTool(Tool.DOCKER);
    doExecute(builder, userId);
  }

  private void doExecute(Builder builder, String userId) {
    final InstallationRequest installationRequest = InstallationRequest.newBuilder()
        .setUserId(userId).setInstallation(builder.build()).build();

    final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
        .create();

    installationRequestService
        .createInstallationRequestAsync(installationRequest, futureResponseCallback);
    try {
      futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          "LanceInstallationStrategy was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error during installation", e.getCause());
    }
  }
}
