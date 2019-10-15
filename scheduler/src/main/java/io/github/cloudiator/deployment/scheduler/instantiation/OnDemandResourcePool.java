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

package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.messaging.NodeCandidateConverter;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestMessage.Builder;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnDemandResourcePool implements ResourcePool {

  private final NodeService nodeService;
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(OnDemandResourcePool.class);
  private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public OnDemandResourcePool(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Override
  public List<ListenableFuture<Node>> allocate(Schedule schedule,
      Iterable<? extends NodeCandidate> nodeCandidates, @Nullable String name) {

    return allocate(schedule, nodeCandidates, Collections.emptyList(), name);

  }

  @Override
  public List<ListenableFuture<Node>> allocate(Schedule schedule,
      Iterable<? extends NodeCandidate> nodeCandidates, Iterable<? extends Node> existingNodes,
      @Nullable String name) {

    LOGGER.info(
        String.format(
            "%s is allocating resources matching node candidates %s with existing resources %s.",
            this,
            Joiner.on(",").join(nodeCandidates), existingNodes));

    final List<NodeCandidate> toStart = calculateDiff(nodeCandidates, existingNodes);

    LOGGER.info(
        String.format(
            "After calculating the diff, %s will spawn the nodes %s",
            this,
            Joiner.on(",").join(toStart)));

    final List<ListenableFuture<Node>> nodeFutures = new ArrayList<>(toStart.size());
    for (NodeCandidate nodeCandidate : toStart) {

      final Builder builder = NodeRequestMessage.newBuilder().setUserId(schedule.userId())
          .setNodeCandidate(nodeCandidate.id());
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
    return nodeFutures;
  }

  private List<NodeCandidate> calculateDiff(Iterable<? extends NodeCandidate> nodeCandidates,
      Iterable<? extends Node> existingNodes) {

    List<Node> nodes = Lists.newArrayList(existingNodes);
    List<NodeCandidate> toSpawn = Lists.newLinkedList();
    for (NodeCandidate nodeCandidate : nodeCandidates) {
      final Optional<Node> any = nodes.stream().filter(new Predicate<Node>() {
        @Override
        public boolean test(Node node) {
          return node.nodeCandidate().isPresent() && node.nodeCandidate().get()
              .equals(nodeCandidate.id());
        }
      }).findAny();
      if (any.isPresent()) {
        nodes.remove(any.get());
      } else {
        toSpawn.add(nodeCandidate);
      }
    }

    return toSpawn;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
