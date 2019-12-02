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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.LinkedList;
import java.util.List;
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
  private static final LoggingScheduledThreadPoolExecutor EXECUTOR = new LoggingScheduledThreadPoolExecutor(
      5);
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleDeletionStrategy.class);
  private final JobMessageRepository jobMessageRepository;
  private final PeriodicScheduler periodicScheduler;
  private final ScheduleDomainRepository scheduleDomainRepository;

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  @Inject
  public ScheduleDeletionStrategy(ProcessService processService,
      NodeService nodeService,
      JobMessageRepository jobMessageRepository,
      PeriodicScheduler periodicScheduler,
      ScheduleDomainRepository scheduleDomainRepository) {
    this.processService = processService;
    this.nodeService = nodeService;
    this.jobMessageRepository = jobMessageRepository;
    this.periodicScheduler = periodicScheduler;
    this.scheduleDomainRepository = scheduleDomainRepository;
  }

  @Transactional
  protected Schedule refresh(Schedule schedule) {
    return scheduleDomainRepository.findByIdAndUser(schedule.id(), schedule.userId());
  }

  private final class DeleteNodesOnProcessDelete implements FutureCallback<ProcessDeletedResponse> {

    private final CloudiatorProcess cloudiatorProcess;
    private final CountDownLatch countDownLatch;

    private DeleteNodesOnProcessDelete(
        CloudiatorProcess cloudiatorProcess, CountDownLatch countDownLatch) {
      this.cloudiatorProcess = cloudiatorProcess;
      this.countDownLatch = countDownLatch;
    }

    @Override
    public void onSuccess(@Nullable ProcessDeletedResponse deleted) {

      List<SettableFuture<NodeDeleteResponseMessage>> futures = new LinkedList<>();

      LOGGER.info(String
          .format("Deleting nodes for process %s. Deleted nodes will be %s.", cloudiatorProcess,
              cloudiatorProcess.nodes()));

      for (String node : cloudiatorProcess.nodes()) {
        final NodeDeleteMessage nodeDeleteMessage = NodeDeleteMessage.newBuilder()
            .setNodeId(node).setUserId(cloudiatorProcess.userId())
            .build();

        SettableFutureResponseCallback<NodeDeleteResponseMessage, NodeDeleteResponseMessage> nodeFuture = SettableFutureResponseCallback
            .create();

        nodeService.deleteNodeAsync(nodeDeleteMessage, nodeFuture);
      }

      final ListenableFuture<List<NodeDeleteResponseMessage>> nodesFuture = Futures
          .successfulAsList(futures);

      try {
        nodesFuture.get();
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted while waiting for node deletion. This may cause illegal state.");
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        LOGGER.error(String
                .format("Error while waiting for nodes of process %s to be deleted", cloudiatorProcess),
            e.getCause());
        throw new IllegalStateException(String
            .format("Error while waiting for nodes of process %s to be deleted",
                cloudiatorProcess));
      } finally {
        countDownLatch.countDown();
      }

    }

    @Override
    public void onFailure(Throwable t) {
      LOGGER.error(String.format("Error while deleting process %s. Will be tried again later.",
          cloudiatorProcess));
      countDownLatch.countDown();
    }
  }

  private void cancelPeriodic(Schedule schedule, CloudiatorProcess cloudiatorProcess) {

    //stop the periodic scheduler for any processes of this schedule
    Job job = jobMessageRepository.getById(schedule.userId(), schedule.job());
    checkState(job != null, "job is null");
    Task task = job.getTask(cloudiatorProcess.taskId())
        .orElseThrow(() -> new IllegalStateException("Task not present in job"));

    periodicScheduler.cancel(task, true);
  }


  private void deleteProcess(Schedule schedule, CloudiatorProcess cloudiatorProcess) {

    cancelPeriodic(schedule, cloudiatorProcess);

    final DeleteProcessRequest deleteProcessRequest = DeleteProcessRequest.newBuilder()
        .setProcessId(cloudiatorProcess.id())
        .setUserId(cloudiatorProcess.userId()).build();

    SettableFutureResponseCallback<ProcessDeletedResponse, ProcessDeletedResponse> processFuture = SettableFutureResponseCallback
        .create();

    LOGGER.info("Issuing request to delete process " + cloudiatorProcess);

    processService.deleteProcessAsync(deleteProcessRequest, processFuture);
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Futures.addCallback(processFuture, new DeleteNodesOnProcessDelete(cloudiatorProcess,
        countDownLatch), EXECUTOR);

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      LOGGER.warn(
          "Interrupted while waiting for process and node deletion. This may cause illegal state.");
      Thread.currentThread().interrupt();
    }

  }

  public void delete(Schedule schedule, String userId) {

    checkNotNull(schedule, "schedule is null");
    checkNotNull(userId, "userId is null");

    LOGGER.info("Starting the deletion of schedule " + schedule);

    //delete all processes
    int totalAttempts = 3;
    int currentAttempt = 1;

    while (!schedule.processes().isEmpty() && currentAttempt <= totalAttempts) {

      LOGGER.debug(String.format("Deleting a total amount of %s processes for schedule %s.",
          schedule.processes().size(), schedule));

      for (CloudiatorProcess cloudiatorProcess : schedule.processes()) {
        deleteProcess(schedule, cloudiatorProcess);
      }
      schedule = refresh(schedule);
      currentAttempt++;
    }


  }

}
