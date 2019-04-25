/*
 * Copyright 2014-2018 University of Ulm
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
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClientRegistryWrapper;
import de.uniulm.omi.cloudiator.lance.lca.DeploymentException;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.registry.RegistrationException;
import io.github.cloudiator.domain.Node;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLanceProcessStrategy {

  private final LanceClientConnector lanceClientConnector;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteLanceProcessStrategy.class);

  @Inject
  public DeleteLanceProcessStrategy(
      LanceClientConnector lanceClientConnector) {
    this.lanceClientConnector = lanceClientConnector;
  }


  public void execute(String processId, String schedule, String taskId, String jobId, Node node) {
    LOGGER.debug(String.format("Connecting to lance agent on node %s.", node));

    try {
      final LifecycleClient lifecycleClient = lanceClientConnector
          .getLifecycleClient(node.connectTo().ip());
      lifecycleClient.undeploy(ComponentInstanceId.fromString(processId), false);
    } catch (DeploymentException | IOException e) {

      LOGGER.warn(String
          .format("Deleting process %s failed with exception: %s. Now deleting from registry.",
              processId, e.getMessage()), e);

      try {
        LifecycleClientRegistryWrapper
            .unRegisterInstance(ApplicationInstanceId.fromString(schedule),
                ComponentIdGenerator.generate(jobId, taskId),
                ComponentInstanceId.fromString(processId));
      } catch (RegistrationException | DeploymentException ex) {
        throw new IllegalStateException(
            String.format("Deleting of process %s finally failed.", processId), e);
      }
    }
  }
}
