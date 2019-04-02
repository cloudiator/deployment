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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.deployment.scheduler.exceptions.SchedulingException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestMessage.Builder;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.NodeEntities.NodeRequirements;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.MatchmakingEntities.NodeCandidate;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.MatchmakingService;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnDemandResourcePool implements ResourcePool {

  private final NodeService nodeService;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(OnDemandResourcePool.class);
  private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private final MatchmakingService matchmakingService;

  @Inject
  public OnDemandResourcePool(NodeService nodeService,
      MatchmakingService matchmakingService) {
    this.nodeService = nodeService;
    this.matchmakingService = matchmakingService;
  }

  @Override
  public ListenableFuture<List<Node>> allocate(Schedule schedule,
      Iterable<? extends Requirement> requirements, @Nullable String name)
      throws SchedulingException {

    LOGGER.info(
        String.format("%s is allocating resources matching requirements %s.", this,
            Joiner.on(",").join(requirements)));

    try {
      final List<NodeCandidate> nodeCandidates = matchmaking(requirements, schedule.userId());

      final List<ListenableFuture<Node>> nodeFutures = new ArrayList<>(nodeCandidates.size());
      for (NodeCandidate nodeCandidate : nodeCandidates) {

        final Builder builder = NodeRequestMessage.newBuilder().setUserId(schedule.userId())
            .setNodeCandidate(nodeCandidate);
        if (name != null) {
          builder.setGroupName(name);
        }

        SettableFutureResponseCallback<NodeRequestResponse, Node> nodeCallback = SettableFutureResponseCallback
            .create(
                nodeRequestResponse -> NODE_CONVERTER.applyBack(nodeRequestResponse.getNode()));

        nodeService.createNodeAsync(builder.build(), nodeCallback);

        nodeFutures.add(nodeCallback);
      }

      //noinspection UnstableApiUsage
      return Futures.allAsList(nodeFutures);


    } catch (MatchmakingException e) {
      throw new SchedulingException("Can not schedule due to matchmaking failure", e);
    }
  }

  private List<NodeCandidate> matchmaking(Iterable<? extends Requirement> requirements,
      String userId) throws MatchmakingException {
    LOGGER.debug(String
        .format("%s is calling matchmaking engine to derive configuration for requirements %s.",
            this, requirements));

    NodeRequirements nodeRequirements = NodeRequirements.newBuilder()
        .addAllRequirements(StreamSupport.stream(requirements.spliterator(), false)
            .map(REQUIREMENT_CONVERTER::applyBack).collect(Collectors.toList()))
        .build();

    final MatchmakingResponse matchmakingResponse;
    try {
      matchmakingResponse = matchmakingService.requestMatch(
          MatchmakingRequest.newBuilder()
              .setNodeRequirements(nodeRequirements)
              .setUserId(userId)
              .build());

      LOGGER.debug(String
          .format("%s received matchmaking response for requirements %s. Used solution is %s.",
              this, requirements, matchmakingResponse.getSolution()));

      return matchmakingResponse.getSolution().getNodeCandidatesList();

    } catch (ResponseException e) {
      throw new MatchmakingException("Could not perform matchmaking.", e);
    }

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
