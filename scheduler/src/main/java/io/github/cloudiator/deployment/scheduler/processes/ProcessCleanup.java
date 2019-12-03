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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messages.Process.ProcessDeletedResponse;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessCleanup {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCleanup.class);
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ProcessService processService;
  private final NodeService nodeService;

  @Inject
  public ProcessCleanup(
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessService processService, NodeService nodeService) {
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processService = processService;
    this.nodeService = nodeService;
  }

  public void finishProcess(CloudiatorProcess cloudiatorProcess) {

    LOGGER.info(String.format("Cleaning up process %s as it was finished.", cloudiatorProcess));

    checkNotNull(cloudiatorProcess, "cloudiatorProcess is null");

    checkArgument(cloudiatorProcess.state().equals(ProcessState.FINISHED),
        "process is not in finished state");

    final Schedule schedule = scheduleDomainRepository.findByProcess(cloudiatorProcess);

    if (schedule == null) {
      throw new IllegalStateException(
          String.format("Could not find schedule for process %s.", cloudiatorProcess));
    }

    if (schedule.instantiation().equals(Instantiation.AUTOMATIC)) {

      final SettableFutureResponseCallback<ProcessDeletedResponse, ProcessDeletedResponse> processDeleteCallback = SettableFutureResponseCallback
          .create();
      processService.deleteProcessAsync(
          DeleteProcessRequest.newBuilder().setProcessId(cloudiatorProcess.id())
              .setUserId(cloudiatorProcess.userId()).build(), processDeleteCallback);

      try {
        processDeleteCallback.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new IllegalStateException(
            String.format("Error while deleting process %s", cloudiatorProcess), e);
      }

      for (String node : cloudiatorProcess.nodes()) {

        final SettableFutureResponseCallback<NodeDeleteResponseMessage, NodeDeleteResponseMessage> nodeDeleteCallback = SettableFutureResponseCallback
            .create();

        nodeService.deleteNodeAsync(
            NodeDeleteMessage.newBuilder().setUserId(cloudiatorProcess.userId()).setNodeId(node)
                .build(), new ResponseCallback<NodeDeleteResponseMessage>() {
              @Override
              public void accept(@Nullable NodeDeleteResponseMessage nodeDeleteResponseMessage,
                  @Nullable Error error) {
                if (error != null) {
                  LOGGER.warn(String.format(
                      "Could not delete node %s allocated by process %s. This may leave an inconsistent node behind.",
                      node, cloudiatorProcess));
                }
              }
            });
      }

    } else {
      LOGGER.info(String.format("Skipping clean up of process %s as schedule is not automatic.",
          cloudiatorProcess));
    }


  }

}
