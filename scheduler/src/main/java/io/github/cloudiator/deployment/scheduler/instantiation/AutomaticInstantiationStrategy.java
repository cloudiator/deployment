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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.CloudiatorFutures;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.ProcessGroupMessageConverter;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import io.github.cloudiator.deployment.scheduler.exceptions.SchedulingException;
import io.github.cloudiator.domain.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.ProcessNew;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticInstantiationStrategy implements InstantiationStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AutomaticInstantiationStrategy.class);
  private static final ProcessGroupMessageConverter PROCESS_GROUP_MESSAGE_CONVERTER = new ProcessGroupMessageConverter();
  private static final ExecutorService EXECUTOR = new LoggingThreadPoolExecutor(0,
      2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  private final ResourcePool resourcePool;
  private final ProcessService processService;

  @Inject
  public AutomaticInstantiationStrategy(
      ResourcePool resourcePool, ProcessService processService) {
    this.resourcePool = resourcePool;
    this.processService = processService;
  }


  @Override
  public boolean supports(Instantiation instantiation) {
    return instantiation.equals(Instantiation.AUTOMATIC);
  }

  @Override
  public void instantiate(Schedule schedule, Job job, String userId) throws InstantiationException {

    checkState(supports(schedule.instantiation()),
        String.format("%s does not support instantiation %s.", this, schedule.instantiation()));

    //futures for the process
    final List<Future<ProcessGroup>> processGroupFutures = new ArrayList<>();

    //countdown latch
    CountDownLatch countDownLatch = new CountDownLatch(job.tasks().size());

    //for each task
    for (Iterator<Task> iter = job.tasksInOrder(); iter.hasNext(); ) {
      final Task task = iter.next();

      LOGGER.info(String.format("Allocating the resources for task %s of job %s", task, job));

      //allocate the resources
      final ListenableFuture<List<Node>> allocation;
      try {

        allocation = resourcePool
            .allocate(schedule, task.requirements(job), task.name());

        Futures.addCallback(allocation, new FutureCallback<List<Node>>() {
          @Override
          public void onSuccess(List<Node> nodes) {

            LOGGER.info(String.format("Nodes %s were created successfully. Starting processes.",
                nodes));

            //spawn processes on success
            //for (Node node : result.getNodes()) {

            LOGGER.info(String
                .format("Requesting new process for schedule %s, task %s on nodes %s.", schedule,
                    task, nodes));

            final ProcessNew newProcess = ProcessNew.newBuilder().setSchedule(
                schedule.id()).setTask(task.name())
                .addAllNodes(nodes.stream().map(Identifiable::id).collect(Collectors.toList()))
                .build();
            final CreateProcessRequest createProcessRequest = CreateProcessRequest
                .newBuilder()
                .setUserId(userId).setProcess(newProcess).build();

            final SettableFutureResponseCallback<ProcessCreatedResponse, ProcessGroup> processGroupFuture = SettableFutureResponseCallback
                .create(processCreatedResponse -> PROCESS_GROUP_MESSAGE_CONVERTER
                    .apply(processCreatedResponse.getProcessGroup()));

            processService.createProcessAsync(createProcessRequest, processGroupFuture);

            processGroupFutures.add(processGroupFuture);

            //}

            countDownLatch.countDown();
          }

          @Override
          public void onFailure(Throwable t) {
            //todo: what to do when nodes fail?
            LOGGER.error(String
                    .format("An uncaught exception occurred while spawning nodes: %s.", t.getMessage()),
                t);

            countDownLatch.countDown();

          }
        }, EXECUTOR);

      } catch (SchedulingException e) {
        throw new InstantiationException("Error during scheduling.", e);
      }

    }

    try {

      LOGGER.info("Waiting for knowledge about final process sizes.");
      countDownLatch.await();

      CloudiatorFutures.waitForFutures(processGroupFutures);


    } catch (InterruptedException e) {
      LOGGER.error("Execution got interrupted. Stopping.");
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOGGER.error("Exception during instantiation.", e);
      throw new InstantiationException("Exception during instantiation.", e);
    }

    LOGGER.info(String.format("Finished instantiation of job %s.", job));

  }

}
