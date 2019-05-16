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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.CloudiatorFutures;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.ServiceBehaviour;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.exceptions.MatchmakingException;
import io.github.cloudiator.deployment.scheduler.instantiation.DependencyGraph.Dependencies;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.NodeCluster;
import org.cloudiator.messages.entities.ProcessEntities.ProcessNew;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticInstantiationStrategy implements InstantiationStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AutomaticInstantiationStrategy.class);
  private static final ExecutorService EXECUTOR = new LoggingThreadPoolExecutor(0,
      2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());
  private final MatchmakingEngine matchmakingEngine;

  static {
    MoreExecutors.addDelayedShutdownHook(EXECUTOR, 5, TimeUnit.MINUTES);
  }

  private final ResourcePool resourcePool;
  private final ProcessService processService;
  private final JobMessageRepository jobMessageRepository;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final PeriodicScheduler periodicScheduler;

  @Inject
  public AutomaticInstantiationStrategy(
      MatchmakingEngine matchmakingEngine,
      ResourcePool resourcePool, ProcessService processService,
      JobMessageRepository jobMessageRepository,
      ScheduleDomainRepository scheduleDomainRepository,
      PeriodicScheduler periodicScheduler) {
    this.matchmakingEngine = matchmakingEngine;
    this.resourcePool = resourcePool;
    this.processService = processService;
    this.jobMessageRepository = jobMessageRepository;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.periodicScheduler = periodicScheduler;
  }

  private ListenableFuture<CloudiatorProcess> submitProcess(Schedule schedule,
      ProcessNew processNew) {
    final CreateProcessRequest createProcessRequest = CreateProcessRequest
        .newBuilder()
        .setUserId(schedule.userId()).setProcess(processNew).build();

    final SettableFutureResponseCallback<ProcessCreatedResponse, CloudiatorProcess> processRequestFuture = SettableFutureResponseCallback
        .create(processCreatedResponse -> ProcessMessageConverter.INSTANCE
            .apply(processCreatedResponse.getProcess()));

    processService.createProcessAsync(createProcessRequest, processRequestFuture);

    return processRequestFuture;
  }


  private abstract class NodeCallback<T> implements FutureCallback<T> {

    private final Schedule schedule;
    private final Task task;
    private final TaskInterface taskInterface;
    private final Runnable beforeExecute;
    private final Runnable afterExecute;
    private final Consumer<CloudiatorProcess> onSuccess;

    protected NodeCallback(Schedule schedule, Task task,
        TaskInterface taskInterface, Runnable beforeExecute, Runnable afterExecute,
        Consumer<CloudiatorProcess> onSuccess) {
      this.schedule = schedule;
      this.task = task;
      this.taskInterface = taskInterface;
      this.beforeExecute = beforeExecute;
      this.afterExecute = afterExecute;
      this.onSuccess = onSuccess;
    }

    @Override
    public final void onSuccess(T result) {
      try {
        beforeExecute.run();
        final CloudiatorProcess cloudiatorProcess = doSuccess(result).get();
        onSuccess.accept(cloudiatorProcess);
      } catch (InterruptedException e) {
        throw new IllegalStateException("Interrupted while waiting for result.", e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Unexpected exception while waiting for result",
            e.getCause());
      } finally {
        afterExecute.run();
      }
    }

    @Override
    public final void onFailure(Throwable t) {
      try {
        beforeExecute.run();
        doFailure(t);
      } finally {
        afterExecute.run();
      }
    }

    public abstract Future<CloudiatorProcess> doSuccess(@Nullable T result);

    public abstract void doFailure(Throwable t);

    public Schedule schedule() {
      return schedule;
    }

    public Task task() {
      return task;
    }

    public TaskInterface taskInterface() {
      return taskInterface;
    }
  }

  private class SingleNodeCallback extends NodeCallback<Node> {

    private SingleNodeCallback(Schedule schedule, Task task,
        TaskInterface taskInterface, Runnable beforeExecute, Runnable afterExecute,
        Consumer<CloudiatorProcess> onSuccess) {
      super(schedule, task, taskInterface, beforeExecute, afterExecute, onSuccess);
    }

    @Override
    public Future<CloudiatorProcess> doSuccess(Node result) {
      LOGGER.info(String.format("Node %s was created. Starting processes.",
          result));

      final ProcessNew build = ProcessNew.newBuilder().setSchedule(
          schedule().id()).setTask(task().name())
          .setTaskInterface(taskInterface().getClass().getCanonicalName())
          .setNode(result.id()).build();

      return submitProcess(schedule(), build);
    }

    @Override
    public void doFailure(Throwable t) {
      throw new IllegalStateException("Unexpected exception while spawning node", t);
    }

  }

  private class ClusterCallback extends NodeCallback<List<Node>> {

    private ClusterCallback(Schedule schedule, Task task,
        TaskInterface taskInterface, Runnable beforeExecute, Runnable afterExecute,
        Consumer<CloudiatorProcess> onSuccess) {
      super(schedule, task, taskInterface, beforeExecute, afterExecute, onSuccess);
    }

    @Override
    public Future<CloudiatorProcess> doSuccess(List<Node> result) {
      LOGGER.info(String.format("Nodes %s were created. Starting processes.",
          result));

      final ProcessNew build = ProcessNew.newBuilder().setSchedule(
          schedule().id()).setTask(task().name())
          .setTaskInterface(taskInterface().getClass().getCanonicalName())
          .setCluster(NodeCluster.newBuilder()
              .addAllNodes(result.stream().map(Identifiable::id).collect(Collectors.toList()))
              .build()).build();

      return submitProcess(schedule(), build);
    }

    @Override
    public void doFailure(Throwable t) {
      throw new IllegalStateException("Unexpected exception while spawning node", t);
    }

  }

  @Override
  public Instantiation supports() {
    return Instantiation.AUTOMATIC;
  }

  @Override
  public Future<Collection<CloudiatorProcess>> deployTask(Task task,
      TaskInterface taskInterface,
      Schedule schedule,
      List<ListenableFuture<Node>> allocatedResources,
      Dependencies dependencies) {

    CountDownLatch countDownLatch;
    final HashSet<CloudiatorProcess> processes = new HashSet<>();

    Consumer<CloudiatorProcess> onSuccessConsumer = new Consumer<CloudiatorProcess>() {
      @Override
      public void accept(CloudiatorProcess cloudiatorProcess) {
        processes.add(cloudiatorProcess);
      }
    };

    switch (taskInterface.processMapping()) {
      case CLUSTER:
        countDownLatch = new CountDownLatch(1);
        final ListenableFuture<List<Node>> nodeFutures = Futures.allAsList(allocatedResources);
        Futures.addCallback(nodeFutures,
            new ClusterCallback(schedule, task, taskInterface, () -> {
              try {
                dependencies.await();
              } catch (InterruptedException e) {
                throw new IllegalStateException(e);
              }
            }, () -> {
              countDownLatch.countDown();
              dependencies.fulfill();
            }, onSuccessConsumer),
            EXECUTOR);
        break;
      case SINGLE:
        countDownLatch = new CountDownLatch(allocatedResources.size());
        for (ListenableFuture<Node> nodeFuture : allocatedResources) {
          Futures.addCallback(nodeFuture,
              new SingleNodeCallback(schedule, task, taskInterface, () -> {
                try {
                  dependencies.await();
                } catch (InterruptedException e) {
                  throw new IllegalStateException(e);
                }
              }, () -> {
                countDownLatch.countDown();
                if (countDownLatch.getCount() == 0) {
                  dependencies.fulfill();
                }
              }, onSuccessConsumer),
              EXECUTOR);
        }
        break;
      default:
        throw new AssertionError("Unknown process mapping " + taskInterface.processMapping());
    }

    return new WaitLockImpl<>(countDownLatch, processes);
  }

  @Override
  public Schedule instantiate(Schedule schedule) throws InstantiationException {

    Job job = jobMessageRepository.getById(schedule.userId(), schedule.job());

    checkNotNull(job, "Schedule is for job %s, but this job does not exist.", schedule.job());

    checkState(supports().equals(schedule.instantiation()),
        String.format("%s does not support instantiation %s.", this, schedule.instantiation()));

    Map<Task, Future<Collection<CloudiatorProcess>>> taskFutureMap = new HashMap<>();

    final Map<Task, TaskInterface> taskInterfaceSelection = new TaskInterfaceSelection()
        .select(job);
    final DependencyGraph dependencyGraph = DependencyGraph.of(job, taskInterfaceSelection);

    //for each task
    for (Iterator<Task> iter = job.tasksInOrder(); iter.hasNext(); ) {
      final Task task = iter.next();

      if (task.behaviour() instanceof ServiceBehaviour) {
        LOGGER.info(String.format("Allocating the resources for task %s of job %s", task, job));

        try {

          final List<NodeCandidate> matchmakingResult = matchmakingEngine
              .matchmaking(task.requirements(job), Collections.emptyList(), schedule.userId());

          final List<ListenableFuture<Node>> allocate = resourcePool
              .allocate(schedule, matchmakingResult, task.name());

          taskFutureMap
              .put(task, deployTask(task, taskInterfaceSelection.get(task), schedule, allocate,
                  dependencyGraph.forTask(task)));

        } catch (MatchmakingException e) {
          throw new InstantiationException("Error while scheduling.", e);
        }
      } else if (task.behaviour() instanceof PeriodicBehaviour) {
        periodicScheduler.schedule(job, task, schedule);
      } else {
        throw new AssertionError("Unknown behaviour type " + task.behaviour());
      }
    }
    LOGGER.info(String
        .format("Waiting for a total of %s tasks to finish.",
            taskFutureMap.size()));

    try {
      CloudiatorFutures.waitForFutures(taskFutureMap.values());
    } catch (ExecutionException | InterruptedException e) {
      throw new InstantiationException("Instantiation failed.", e);
    }

    //refresh the schedule object to receive the created processes
    Schedule createdSchedule = refresh(schedule);

    checkState(createdSchedule != null, String
        .format("Expected schedule with id %s to exist, but received null", schedule.id()));

    createdSchedule.setState(ScheduleState.RUNNING);

    LOGGER.info(String.format("Schedule %s was instantiated.", schedule));

    return createdSchedule;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule refresh(Schedule schedule) {
    return scheduleDomainRepository
        .findByIdAndUser(schedule.id(), schedule.userId());
  }

}
