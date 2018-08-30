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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.CreateScheduleRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.Process.ScheduleCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.ProcessNew;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleRequestSubscriber implements Runnable {

  private final ProcessService processService;
  private final JobMessageRepository jobMessageRepository;
  private final MessageInterface messageInterface;
  private final ResourcePool resourcePool;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleRequestSubscriber.class);
  private static final LoggingScheduledThreadPoolExecutor EXECUTOR = new LoggingScheduledThreadPoolExecutor(
      5);
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final ScheduleConverter SCHEDULE_CONVERTER = ScheduleConverter.INSTANCE;

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  @Inject
  public ScheduleRequestSubscriber(ProcessService processService,
      JobMessageRepository jobMessageRepository,
      MessageInterface messageInterface,
      ResourcePool resourcePool) {
    this.processService = processService;
    this.jobMessageRepository = jobMessageRepository;
    this.messageInterface = messageInterface;
    this.resourcePool = resourcePool;
  }

  @Override
  public void run() {
    processService.subscribeSchedule(new MessageCallback<CreateScheduleRequest>() {
      @Override
      public void accept(String id, CreateScheduleRequest content) {

        try {

          final String jobId = content.getSchedule().getJob();
          final Instantiation instantiation = Instantiation
              .valueOf(content.getSchedule().getInstantiaton());
          final String userId = content.getUserId();

          //retrieve the job
          Job job = jobMessageRepository.getById(userId, jobId);
          if (job == null) {
            messageInterface.reply(ScheduleCreatedResponse.class, id,
                Error.newBuilder().setCode(404).setMessage("Could not find job with id " + jobId)
                    .build());
            return;
          }

          Schedule schedule = ScheduleImpl.create(job, instantiation);

          //for each task
          for (Task task : job.tasks()) {

            //allocate the resources
            final ListenableFuture<NodeGroup> allocateFuture = resourcePool
                .allocate(userId, task.requirements(), task.name());

            //futures for the process
            final Set<ListenableFuture<CloudiatorProcess>> processFutures = new HashSet<>();

            Futures.addCallback(allocateFuture, new FutureCallback<NodeGroup>() {
              @Override
              public void onSuccess(@Nullable NodeGroup result) {

                //spawn processes on success
                for (Node node : result.getNodes()) {
                  final ProcessNew newProcess = ProcessNew.newBuilder().setSchedule(
                      schedule.id()).setTask(task.name())
                      .setNode(node.id())
                      .build();
                  final CreateProcessRequest createProcessRequest = CreateProcessRequest
                      .newBuilder()
                      .setUserId(userId).setProcess(newProcess).build();

                  final SettableFutureResponseCallback<ProcessCreatedResponse, CloudiatorProcess> responseCallback = SettableFutureResponseCallback
                      .create(processCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                          .apply(processCreatedResponse.getProcess()));

                  processService.createProcessAsync(createProcessRequest, responseCallback);

                  processFutures.add(responseCallback);

                }
              }

              @Override
              public void onFailure(Throwable t) {
                //todo: what to do when nodes fail?
                LOGGER.error("Error while spawning nodes");
              }
            }, EXECUTOR);

            final ListenableFuture<List<CloudiatorProcess>> processes = Futures
                .successfulAsList(processFutures);

            final List<CloudiatorProcess> cloudiatorProcesses = processes.get();

            if (cloudiatorProcesses.contains(null)) {
              //todo: reply with error
              LOGGER.error("One or more processes failed to start");
            }

            schedule.addProcesses(cloudiatorProcesses.stream().filter(Objects::nonNull)
                .collect(Collectors.toList()));

            messageInterface.reply(id,
                ScheduleCreatedResponse.newBuilder().setSchedule(SCHEDULE_CONVERTER.apply(schedule))
                    .build());
          }
        } catch (Exception e) {
          //todo: reply with error
          LOGGER.error("Unexpected exception while handling schedule.", e);
        }

      }
    });
  }
}
