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

package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import io.github.cloudiator.deployment.graph.Graphs;
import io.github.cloudiator.deployment.graph.ScheduleGraph;
import io.github.cloudiator.domain.Node;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleImpl implements Schedule {

  private final String id;
  private final String userId;
  private final String job;
  private final Set<CloudiatorProcess> processes;
  private final Instantiation instantiation;
  private ScheduleState scheduleState;

  private ScheduleImpl(String id, String userId, String job,
      Instantiation instantiation, ScheduleState scheduleState) {

    checkNotNull(id, "id is null");
    checkNotNull(userId, "userId is null");
    checkNotNull(job, "job is null");
    checkNotNull(instantiation, "instantiation is null");
    checkNotNull(scheduleState, "scheduleState is null");

    this.id = id;
    this.job = job;
    this.userId = userId;
    this.instantiation = instantiation;
    this.processes = new HashSet<>();
    this.scheduleState = scheduleState;
  }

  public static Schedule init(Job job, Instantiation instantiation) {
    return new ScheduleImpl(UUID.randomUUID().toString(), job.userId(), job.id(), instantiation,
        ScheduleState.PENDING);
  }

  public static Schedule of(String id, String userId, String jobId,
      Instantiation instantiation, ScheduleState scheduleState) {
    return new ScheduleImpl(id, userId, jobId, instantiation, scheduleState);
  }

  @Override
  public String job() {
    return job;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public Set<CloudiatorProcess> processes() {
    return ImmutableSet.copyOf(processes);
  }

  @Override
  public Instantiation instantiation() {
    return instantiation;
  }

  @Override
  public Schedule addProcess(CloudiatorProcess cloudiatorProcess) {
    processes.add(cloudiatorProcess);
    return this;
  }

  @Override
  public Schedule addProcesses(Collection<? extends CloudiatorProcess> processes) {
    this.processes.addAll(processes);
    return null;
  }

  @Override
  public ScheduleState state() {
    return scheduleState;
  }

  @Override
  public Schedule setState(ScheduleState scheduleState) {
    this.scheduleState = scheduleState;
    return this;
  }

  @Override
  public boolean runsOnNode(Node node) {

    for (CloudiatorProcess cloudiatorProcess : processes) {
      if (cloudiatorProcess.nodes().contains(node.id())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Task getTask(CloudiatorProcess cloudiatorProcess, Job job) {

    return job.getTask(cloudiatorProcess.taskId()).orElseThrow(() -> new IllegalArgumentException(
        String.format("Job %s does not contain the task the process %s is referencing.", job,
            cloudiatorProcess)));

  }

  @Override
  public Set<CloudiatorProcess> processesForTask(Task task) {
    return processes.stream().filter(
        cloudiatorProcess -> cloudiatorProcess.taskId().equals(task.name()))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<CloudiatorProcess> processesForNode(Node node) {

    Set<CloudiatorProcess> processesForNode = new HashSet<>();
    for (CloudiatorProcess cloudiatorProcess : processes) {
      if (cloudiatorProcess.nodes().contains(node.id())) {
        processesForNode.add(cloudiatorProcess);
      }
    }
    return ImmutableSet.copyOf(processesForNode);
  }

  @Override
  public Set<String> nodes() {
    return processes().stream().flatMap(
        (Function<CloudiatorProcess, Stream<String>>) cloudiatorProcess -> cloudiatorProcess.nodes()
            .stream()).collect(Collectors.toSet());
  }

  @Override
  public void notifyOfProcess(Job job, CloudiatorProcess cloudiatorProcess,
      TaskUpdater taskUpdater) {
    final ScheduleGraph scheduleGraph = Graphs.scheduleGraph(this, job);
    final List<CloudiatorProcess> dependentProcesses = scheduleGraph
        .getDependentProcesses(cloudiatorProcess);

    for (CloudiatorProcess dependent : dependentProcesses) {
      Task task = job.getTask(dependent.taskId()).orElseThrow(IllegalStateException::new);
      final TaskInterface taskInterface = task.interfaceOfName(dependent.taskInterface());

      if (taskUpdater.supports(taskInterface)) {
        taskUpdater.update(this, job, taskInterface, task, cloudiatorProcess);
      }
    }
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("userId", userId).add("job", job)
        .add("instantiation", instantiation).toString();
  }
}
