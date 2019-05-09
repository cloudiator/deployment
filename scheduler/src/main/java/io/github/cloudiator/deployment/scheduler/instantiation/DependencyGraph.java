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

import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.graph.JobGraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {

  private final Job job;
  private final Map<Task, TaskLock> locks = new HashMap<>();
  private final Map<Task, TaskInterface> selectedInterfaces;

  private DependencyGraph(Job job, Map<Task, TaskInterface> selectedInterfaces) {

    this.job = job;
    for (Task task : job.tasks()) {
      locks.put(task, new TaskLock(task));
    }

    this.selectedInterfaces = selectedInterfaces;


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
        if (fulfilled) {
          return;
        }
      }
      this.wait();
    }
  }


  public Dependencies forTask(Task task) {

    final JobGraph jobGraph = JobGraph.of(job);
    final HashSet<DownStream> downStreams = new HashSet<>();

    for (Task dependency : jobGraph.getDependencies(task)) {

      //check if we need to wait here
      final TaskInterface dependencyInterface = selectedInterfaces.get(dependency);
      final TaskInterface myInterface = selectedInterfaces.get(task);

      if (myInterface.requiresManualWait(dependencyInterface)) {
        downStreams.add(new DownStream(locks.get(dependency)));
      }

    }

    return new Dependencies(downStreams, new UpStream(locks.get(task)));
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
  }

  public static class UpStream implements Dependency {

    private final TaskLock taskLock;

    public UpStream(TaskLock taskLock) {
      this.taskLock = taskLock;
    }

    public void fulfill() {
      taskLock.fulfill();
    }

    @Override
    public Task getTask() {
      return taskLock.getTask();
    }
  }

  public static class Dependencies {

    private final Set<DownStream> dependencies;
    private final UpStream upStream;

    private Dependencies(Set<DownStream> dependencies, UpStream upStream) {
      this.dependencies = dependencies;
      this.upStream = upStream;
    }

    public void await() throws InterruptedException {
      for (DownStream downStream : dependencies) {
        downStream.await();
      }
    }

    public void fulfill() {
      upStream.fulfill();
    }

  }


}
