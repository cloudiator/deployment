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

package io.github.cloudiator.deployment.scheduler.instantiation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Singleton
public class PeriodicScheduler {

  private final ExecutionService executionService;
  private final PeriodicBehaviourSchedulableFactory factory;
  private final Map<Task, Future<?>> taskFutures;

  @Inject
  public PeriodicScheduler(
      PeriodicBehaviourSchedulableFactory factory) {
    this.factory = factory;
    final LoggingScheduledThreadPoolExecutor loggingScheduledThreadPoolExecutor = new LoggingScheduledThreadPoolExecutor(
        5);
    executionService = new ScheduledThreadPoolExecutorExecutionService(
        loggingScheduledThreadPoolExecutor);
    MoreExecutors.addDelayedShutdownHook(loggingScheduledThreadPoolExecutor, 1, TimeUnit.MINUTES);
    taskFutures = new HashMap<>();
  }


  public void schedule(Job job, Task task, Schedule schedule) {

    checkArgument(task.behaviour() instanceof PeriodicBehaviour,
        String.format("Task %s does not provide a periodic behaviour", task));

    final PeriodicBehaviourSchedulable periodicBehaviourSchedulable = factory
        .create(job, task, schedule, executionService);

    taskFutures.put(task, executionService.schedule(periodicBehaviourSchedulable));
  }

  public void cancel(Task task, boolean force) {

    final Future<?> future = taskFutures.get(task);
    if (future == null) {
      return;
    }

    boolean result = future.cancel(force);
    if (result) {
      taskFutures.remove(task);
    }

  }

}
