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

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestMessage.Builder;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.NodeEntities.NodeRequirements;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnDemandResourcePool implements ResourcePool {

  private final NodeService nodeService;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(OnDemandResourcePool.class);
  private static final NodeGroupMessageToNodeGroup NODE_GROUP_CONVERTER = new NodeGroupMessageToNodeGroup();

  @Inject
  public OnDemandResourcePool(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Override
  public ListenableFuture<NodeGroup> allocate(String userId,
      Iterable<? extends Requirement> requirements, @Nullable String name) {

    LOGGER.info(
        String.format("%s is allocating resources matching requirements %s.", this,
            Joiner.on(",").join(requirements)));

    final NodeRequirements nodeRequirements = NodeRequirements.newBuilder()
        .addAllRequirements(StreamSupport.stream(requirements.spliterator(), false)
            .map(REQUIREMENT_CONVERTER::applyBack).collect(
                Collectors.toList())).build();

    final Builder builder = NodeRequestMessage.newBuilder();
    builder.setUserId(userId).setRequirements(nodeRequirements);
    if (name != null) {
      builder.setGroupName(name);
    }

    final NodeRequestMessage requestMessage = builder.build();

    SettableFutureResponseCallback<NodeRequestResponse, NodeGroup> futureResponseCallback = SettableFutureResponseCallback
        .create(
            nodeRequestResponse -> NODE_GROUP_CONVERTER.apply(nodeRequestResponse.getNodeGroup()));

    LOGGER.info(
        String.format("%s issued node request message %s.", this,
            requestMessage));
    nodeService.createNodesAsync(requestMessage, futureResponseCallback);

    return futureResponseCallback;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
