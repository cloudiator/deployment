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

import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.messages.Process.ProcessStatusQuery;
import org.cloudiator.messages.Process.ProcessStatusQuery.Builder;
import org.cloudiator.messaging.services.ProcessService;

public class ProcessStatusCheckerImpl implements ProcessStatusChecker {

  private final NodeMessageRepository nodeMessageRepository;
  private final ProcessService processService;

  public ProcessStatusCheckerImpl(
      NodeMessageRepository nodeMessageRepository,
      ProcessService processService) {
    this.nodeMessageRepository = nodeMessageRepository;
    this.processService = processService;
  }

  @Override
  public ProcessState checkState(CloudiatorProcess cloudiatorProcess) {

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
          ((CloudiatorClusterProcess) cloudiatorProcess).nodes().size());
      for (String node)


    } else {
      throw new AssertionError("Unknown process type " + cloudiatorProcess.getClass().getName());
    }

    processService.queryProcessStatus();

  }
}
