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

package io.github.cloudiator.deployment.scheduler.processes;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.ProcessStatusQuery;
import org.cloudiator.messages.Process.ProcessStatusQuery.Builder;
import org.cloudiator.messages.Process.ProcessStatusResponse;
import org.cloudiator.messages.entities.ProcessEntities.Nodes;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatusCheckerImpl implements ProcessStatusChecker {


  private final NodeMessageRepository nodeMessageRepository;
  private final ProcessService processService;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProcessStatusCheckerImpl.class);

  @Inject
  public ProcessStatusCheckerImpl(
      NodeMessageRepository nodeMessageRepository,
      ProcessService processService) {
    this.nodeMessageRepository = nodeMessageRepository;
    this.processService = processService;
  }

  @Override
  public ProcessStatus checkState(CloudiatorProcess cloudiatorProcess) throws ResponseException {

    final Builder builder = ProcessStatusQuery.newBuilder()
        .setProcess(ProcessMessageConverter.INSTANCE.applyBack(cloudiatorProcess));

    if (cloudiatorProcess instanceof CloudiatorSingleProcess) {
      final Node byId = nodeMessageRepository.getById(cloudiatorProcess.userId(),
          ((CloudiatorSingleProcess) cloudiatorProcess).node());
      if (byId == null) {
        throw new IllegalStateException(String.format("Node with id %s does not exist.",
            ((CloudiatorSingleProcess) cloudiatorProcess).node()));
      }
      builder.setNode(NodeToNodeMessageConverter.INSTANCE.apply(byId));
    } else if (cloudiatorProcess instanceof CloudiatorClusterProcess) {

      final Set<Node> nodes = new HashSet<>(
          cloudiatorProcess.nodes().size());

      for (String node : cloudiatorProcess.nodes()) {
        final Node byId = nodeMessageRepository.getById(cloudiatorProcess.userId(), node);
        if (byId == null) {
          throw new IllegalStateException(String.format("Node with id %s does not exist.",
              node));
        }
        nodes.add(byId);
      }

      builder.setNodes(Nodes.newBuilder().addAllNodes(nodes.stream().map(
          NodeToNodeMessageConverter.INSTANCE).collect(
          Collectors.toSet())));

    } else {
      throw new AssertionError("Unknown process type " + cloudiatorProcess.getClass().getName());
    }

    final ProcessStatusResponse processStatusResponse = processService
        .queryProcessStatus(builder.build());

    final String information = Strings.emptyToNull(processStatusResponse.getInformation());
    return ProcessStatus.of(ProcessMessageConverter.PROCESS_STATE_CONVERTER
        .apply(processStatusResponse.getState()), information);


  }
}
