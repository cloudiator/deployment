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

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.domain.TaskUpdater;
import io.github.cloudiator.deployment.graph.JobGraph;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyGraph {

  private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

  private final Job job;
  private final Map<Task, TaskLock> locks = new HashMap<>();
  private final Map<Task, TaskInterface> selectedInterfaces;

  private DependencyGraph(Job job, Map<Task, TaskInterface> selectedInterfaces) {

    this.job = job;

    this.selectedInterfaces = selectedInterfaces;

    for (Task task : job.tasks()) {
      locks.put(task, new TaskLock(task));
    }
  }

  public static DependencyGraph of(Job job, Map<Task, TaskInterface> selectedInterfaces) {
    return new DependencyGraph(job, selectedInterfaces);
  }

  private static class TaskLock {

    private final Task task;
    private boolean fulfilled = false;

    private TaskLock(Task task) {
      this.task = task;
    }

    public Task getTask() {
      return task;
    }

    public void fulfill() {
      synchronized (this) {
        fulfilled = true;
        this.notifyAll();
      }
    }

    public void await() throws InterruptedException {
      synchronized (this) {
        if (!fulfilled) {
          this.wait();
        }
      }
    }
  }

  public static Dependencies noDependencies(Job job, Schedule schedule, Task task) {
    return new Dependencies(job, schedule, task, Collections.emptySet(), new UpStream(null));
  }

  public Dependencies forTask(Job job, Schedule schedule, Task task) {

    final JobGraph jobGraph = JobGraph.of(job);
    final HashSet<DownStream> downStreams = new HashSet<>();

    for (Task dependency : jobGraph.getDependencies(task, true)) {

      //check if we need to wait here
      final TaskInterface dependencyInterface = selectedInterfaces.get(dependency);
      final TaskInterface myInterface = selectedInterfaces.get(task);

      if (myInterface.requiresManualWait(dependencyInterface)) {
        downStreams.add(new DownStream(locks.get(dependency)));
      }

    }

    return new Dependencies(job, schedule, task, downStreams, new UpStream(locks.get(task)));
  }

  public interface Dependency {

    Task getTask();

  }

  public static class DownStream implements Dependency {

    private final TaskLock taskLock;

    public DownStream(TaskLock taskLock) {
      this.taskLock = taskLock;
    }

    public void await() throws InterruptedException {
      taskLock.await();
    }

    @Override
    public Task getTask() {
      return taskLock.getTask();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("task", taskLock.getTask())
          .toString();
    }
  }

  public static class UpStream implements Dependency {

    @Nullable
    private final TaskLock taskLock;

    public UpStream(@Nullable TaskLock taskLock) {
      this.taskLock = taskLock;
    }

    public void fulfill() {
      if (taskLock != null) {
        taskLock.fulfill();
      }
    }

    @Override
    @Nullable
    public Task getTask() {
      if (taskLock != null) {
        return taskLock.getTask();
      }
      return null;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("task", getTask())
          .toString();
    }
  }

  public static class Dependencies {

    private final Job job;
    private final Schedule schedule;
    private final Task task;
    private final Set<DownStream> dependencies;
    private final UpStream upStream;

    private Dependencies(Job job, Schedule schedule, Task task, Set<DownStream> dependencies,
        UpStream upStream) {
      this.job = job;
      this.schedule = schedule;
      this.task = task;
      this.dependencies = dependencies;
      this.upStream = upStream;
    }

    public void await() throws InterruptedException {

      LOGGER.info(
          String.format("Task %s is waiting for these downstream components: %s", task,
              Joiner.on(",").join(dependencies)));

      for (DownStream downStream : dependencies) {
        downStream.await();
      }

      LOGGER.info(
          String.format("Task %s has finished waiting for these downstream components: %s", task,
              Joiner.on(",").join(dependencies)));
    }

    public void fulfill(@Nullable CloudiatorProcess cloudiatorProcess, TaskUpdater taskUpdater) {
      LOGGER.info(String.format("Task %s is now fulfilling it's dependencies.", task));
      upStream.fulfill();
      if (cloudiatorProcess != null) {
        schedule.notifyOfProcess(job, cloudiatorProcess, taskUpdater);
      }
    }

  }


}
