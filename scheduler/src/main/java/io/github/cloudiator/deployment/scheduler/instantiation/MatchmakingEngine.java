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

package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.messaging.NodeCandidateConverter;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.messages.NodeEntities.NodeRequirements;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchmakingEngine {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MatchmakingEngine.class);
  private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private final MatchmakingService matchmakingService;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;

  @Inject
  public MatchmakingEngine(MatchmakingService matchmakingService) {
    this.matchmakingService = matchmakingService;
  }

  public List<NodeCandidate> matchmaking(Iterable<? extends Requirement> requirements,
      Iterable<Node> existingResources, @Nullable Integer minimumNodes,
      String userId) throws MatchmakingException {
    LOGGER.debug(String
        .format(
            "%s is calling matchmaking engine to derive configuration for requirements %s with existing nodes %s.",
            this, requirements, existingResources));

    NodeRequirements nodeRequirements = NodeRequirements.newBuilder()
        .addAllRequirements(StreamSupport.stream(requirements.spliterator(), false)
            .map(REQUIREMENT_CONVERTER::applyBack).collect(Collectors.toList()))
        .build();

    int minimumNodeSize;
    if (minimumNodes == null) {
      minimumNodeSize = 0;
    } else {
      minimumNodeSize = minimumNodes;
    }

    final MatchmakingResponse matchmakingResponse;
    try {
      matchmakingResponse = matchmakingService.requestMatch(
          MatchmakingRequest.newBuilder()
              .setNodeRequirements(nodeRequirements)
              .addAllExistingNodes(StreamSupport.stream(existingResources.spliterator(), false).map(
                  NODE_CONVERTER).collect(Collectors.toList()))
              .setUserId(userId)
              .setMinimumNodeSize(minimumNodeSize)
              .build());

      LOGGER.debug(String
          .format("%s received matchmaking response for requirements %s. Used solution is %s.",
              this, requirements, matchmakingResponse.getSolution()));

      return matchmakingResponse.getSolution().getNodeCandidatesList().stream()
          .map(NODE_CANDIDATE_CONVERTER::apply).collect(Collectors.toList());

    } catch (ResponseException e) {
      throw new MatchmakingException("Could not perform matchmaking.", e);
    }

  }

}
