/*
 * Copyright 2014-2018 University of Ulm
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.messaging.NodeGroupMessageRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messages.Process.ProcessDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleDeletionStrategy {

  private final ProcessService processService;
  private final NodeService nodeService;
  private final NodeGroupMessageRepository nodeGroupMessageRepository;
  private static final LoggingScheduledThreadPoolExecutor EXECUTOR = new LoggingScheduledThreadPoolExecutor(
      5);
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleDeletionStrategy.class);

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  @Inject
  public ScheduleDeletionStrategy(ProcessService processService,
      NodeService nodeService, NodeGroupMessageRepository nodeGroupMessageRepository) {
    this.processService = processService;
    this.nodeService = nodeService;
    this.nodeGroupMessageRepository = nodeGroupMessageRepository;
  }

  public void delete(Schedule schedule, String userId) {

    checkNotNull(schedule, "schedule is null");
    checkNotNull(userId, "userId is null");

    LOGGER.info("Starting the deletion of schedule " + schedule);

    LOGGER.debug(String.format("Deleting a total amount of %s processes for schedule %s.",
        schedule.processes().size(), schedule));
    final CountDownLatch countDownLatch = new CountDownLatch(schedule.processes().size());

    //delete all processes
    for (CloudiatorProcess cloudiatorProcess : schedule.processes()) {

      if(cloudiatorProcess instanceof CloudiatorClusterProcess){
        throw  new IllegalStateException("Trying to schedule the deletion of the CloudiatorClusterProcess for Lance, this should never happen!");
      }

      final CloudiatorSingleProcess cloudiatorSingleProcess = (CloudiatorSingleProcess) cloudiatorProcess;

      final DeleteProcessRequest deleteProcessRequest = DeleteProcessRequest.newBuilder()
          .setProcessId(cloudiatorProcess.id())
          .setUserId(userId).build();

      SettableFutureResponseCallback<ProcessDeletedResponse, ProcessDeletedResponse> processFuture = SettableFutureResponseCallback
          .create();

      LOGGER.info("Deleting the process " + cloudiatorProcess);

      processService.deleteProcessAsync(deleteProcessRequest, processFuture);

      Futures.addCallback(processFuture, new FutureCallback<ProcessDeletedResponse>() {
        @Override
        public void onSuccess(@Nullable ProcessDeletedResponse result) {
          //delete the node
          LOGGER.info(String.format(
              "Successfully deleted the process %s. Starting the deletion of the corresponding nodes on nodegroup %s.",
              cloudiatorSingleProcess.id(), cloudiatorSingleProcess.node()));



            final NodeDeleteMessage nodeDeleteMessage = NodeDeleteMessage.newBuilder()
                .setNodeId(((CloudiatorSingleProcess) cloudiatorProcess).node()).setUserId(userId)
                .build();

            SettableFutureResponseCallback<NodeDeleteResponseMessage, NodeDeleteResponseMessage> nodeFuture = SettableFutureResponseCallback
                .create();

            nodeService.deleteNodeAsync(nodeDeleteMessage, nodeFuture);

            try {
              nodeFuture.get();
            } catch (InterruptedException e) {
              throw new IllegalStateException(String
                  .format("Interrupted while deleting node with id %s.", cloudiatorSingleProcess.node()),
                  e);
            } catch (ExecutionException e) {
              throw new IllegalStateException(String
                  .format("Error while deleting node %s of process %s.", cloudiatorSingleProcess.node(),
                      cloudiatorProcess), e);
            } finally {
              countDownLatch.countDown();
            }

            LOGGER.debug(String
                .format("Deleted process %s. %s processes remaining.", cloudiatorProcess,
                    countDownLatch.getCount()));
          }





        @Override
        public void onFailure(Throwable t) {
          LOGGER.error(String.format("Error while deleting process %s.", cloudiatorProcess), t);
          countDownLatch.countDown();
          throw new IllegalStateException(
              String.format("Error while deleting process %s.", cloudiatorProcess), t);
        }
      }, EXECUTOR);


    }

    LOGGER.debug("Waiting for all processes and corresponding nodes to be delete.");
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          "Interrupted while waiting for termination of all processes.");
    }
  }

}
