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

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.InstallationEntities.Installation;
import org.cloudiator.messages.InstallationEntities.Installation.Builder;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.services.InstallationRequestService;

public class LanceInstallationStrategy {

  private final InstallationRequestService installationRequestService;
  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();

  @Inject
  public LanceInstallationStrategy(
      InstallationRequestService installationRequestService) {
    this.installationRequestService = installationRequestService;
  }

  public void execute(String userId, Node node, ContainerType containerType) {

    final Builder builder = Installation.newBuilder()
        .setNode(nodeToNodeMessageConverter.apply(node))
        .addTool(Tool.LANCE);
    if (ContainerType.DOCKER.equals(containerType) || ContainerType.DOCKER_REMOTE
        .equals(containerType)) {
      builder.addTool(Tool.DOCKER);
    }
    final InstallationRequest installationRequest = InstallationRequest.newBuilder()
        .setUserId(userId).setInstallation(builder.build()).build();

    final SettableFuture<Boolean> result = SettableFuture.create();

    installationRequestService.createInstallationRequestAsync(installationRequest,
        new ResponseCallback<InstallationResponse>() {
          @Override
          public void accept(@Nullable InstallationResponse content, @Nullable Error error) {
            if (content != null) {
              result.set(true);
            }
            if (error != null) {
              result.setException(new IllegalStateException(String
                  .format("Exception during installation of tools. Code: %s. Message: %s",
                      error.getCode(), error.getMessage())));
            }
          }
        });
    try {
      result.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          "LanceInstallationStrategy was interrupted during installation request.", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error during installation", e.getCause());
    }
  }
}
