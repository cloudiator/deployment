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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.DeleteLanceProcessRequest;
import org.cloudiator.messages.Process.LanceProcessDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanceProcessKillerImpl implements ProcessKiller {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessKillerImpl.class);
  private final ProcessService processService;
  private final NodeMessageRepository nodeMessageRepository;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();

  @Inject
  public LanceProcessKillerImpl(ProcessService processService,
      NodeMessageRepository nodeMessageRepository) {
    this.processService = processService;
    this.nodeMessageRepository = nodeMessageRepository;
  }

  @Override
  public boolean supports(CloudiatorProcess cloudiatorProcess) {
    return cloudiatorProcess.type().equals(Type.LANCE);
  }

  @Override
  public void kill(String userId, CloudiatorProcess cloudiatorProcess) {

    LOGGER.info(String
        .format("%s is killing the process %s for user: %s", this,
            cloudiatorProcess, userId));

    final Node byId = nodeMessageRepository.getById(userId, cloudiatorProcess.nodeId());

    if (byId == null) {
      throw new IllegalStateException(
          String.format("Could not find node for process %s.", cloudiatorProcess));
    }

    final DeleteLanceProcessRequest deleteLanceProcessRequest = DeleteLanceProcessRequest
        .newBuilder()
        .setProcessId(cloudiatorProcess.id()).setUserId(userId)
        .setNode(NODE_MESSAGE_CONVERTER.apply(byId))
        .build();

    SettableFutureResponseCallback<LanceProcessDeletedResponse, LanceProcessDeletedResponse> futureResponseCallback = SettableFutureResponseCallback
        .create();

    processService.deleteLanceProcessAsync(deleteLanceProcessRequest, futureResponseCallback);

    try {
      futureResponseCallback.get();

      LOGGER.info("%s successfully killed the process %s.", this, cloudiatorProcess, userId);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Got interrupted while waiting for lance process deletion.");
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error while deleting lance process.", e.getCause());
    }

  }
}
