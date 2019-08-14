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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.scheduler.failure.NodeFailureReportingInterface;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messaging.services.NodeService;

public class NodeEventSubscriber implements Runnable {

  private final NodeService nodeService;
  private final NodeFailureReportingInterface nodeFailureReportingInterface;

  @Inject
  public NodeEventSubscriber(NodeService nodeService,
      NodeFailureReportingInterface nodeFailureReportingInterface) {
    this.nodeService = nodeService;
    this.nodeFailureReportingInterface = nodeFailureReportingInterface;
  }

  @Override
  public void run() {
    nodeService.subscribeNodeEvents((id, content) -> {

      NodeState toState = NodeToNodeMessageConverter.NODE_STATE_CONVERTER
          .applyBack(content.getTo());

      //check if node failure
      if (toState.equals(NodeState.ERROR)) {
        nodeFailureReportingInterface
            .addNodeFailure(NodeToNodeMessageConverter.INSTANCE.applyBack(content.getNode()));
      }
    });
  }
}
