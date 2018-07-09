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

package io.github.cloudiator.deployment.scheduler;

import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.NodeEntities.NodeRequirements;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnDemandResourcePool implements ResourcePool {

  private final NodeService nodeService;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(OnDemandResourcePool.class);
  private static final NodeToNodeMessageConverter nodeConverter = new NodeToNodeMessageConverter();

  public OnDemandResourcePool(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Override
  public Iterable<Node> allocate(Iterable<? extends Requirement> requirements) {

    final NodeRequirements nodeRequirements = NodeRequirements.newBuilder()
        .addAllRequirements(StreamSupport.stream(requirements.spliterator(), false)
            .map(REQUIREMENT_CONVERTER::applyBack).collect(
                Collectors.toList())).build();

    final NodeRequestMessage requestMessage = NodeRequestMessage.newBuilder().setGroupName("blub")
        .setNodeRequest(nodeRequirements).build();

    try {
      final NodeRequestResponse nodes = nodeService.createNodes(requestMessage);

      return nodes.getNodeGroup().getNodesList().stream().map(nodeConverter::applyBack)
          .collect(Collectors.toList());


    } catch (ResponseException e) {
      LOGGER.error("Error while allocating nodes", e);
      throw new IllegalStateException("Error while allocating nodes", e);
      //todo handle exception
    }
  }
}
