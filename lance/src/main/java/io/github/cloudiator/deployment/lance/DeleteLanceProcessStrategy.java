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
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import de.uniulm.omi.cloudiator.lance.lca.DeploymentException;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import io.github.cloudiator.domain.Node;
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

  public void execute(String processId, Node node) {

    final LifecycleClient lifecycleClient = lanceClientConnector
        .getLifecycleClient(node.connectTo().ip());

    try {
      //todo make reg deletion parameterizable
      lifecycleClient.undeploy(ComponentInstanceId.fromString(processId), false);
    } catch (DeploymentException e) {
      throw new IllegalStateException("Killing of process %s failed.",e);
    }

  }

}
