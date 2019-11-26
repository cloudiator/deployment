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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.domain.TaskUpdater;
import io.github.cloudiator.deployment.graph.Graphs;
import io.github.cloudiator.deployment.graph.ScheduleGraph;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.exceptions.ProcessDeletionException;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.List;
import java.util.Set;

public class CompositeProcessKiller implements ProcessKiller {

  private final Set<ProcessKiller> processKillers;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final JobMessageRepository jobMessageRepository;
  private final TaskUpdater taskUpdaters;

  @Inject
  public CompositeProcessKiller(
      Set<ProcessKiller> processKillers,
      ScheduleDomainRepository scheduleDomainRepository,
      JobMessageRepository jobMessageRepository,
      TaskUpdater taskUpdaters) {
    this.processKillers = processKillers;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.jobMessageRepository = jobMessageRepository;
    this.taskUpdaters = taskUpdaters;
  }

  @Override
  public boolean supports(CloudiatorProcess cloudiatorProcess) {

    for (ProcessKiller processKiller : processKillers) {
      if (processKiller.supports(cloudiatorProcess)) {
        return true;
      }
    }
    return false;
  }

  protected List<CloudiatorProcess> dependencies(Schedule schedule, Job job,
      CloudiatorProcess cloudiatorProcess) {

    final ScheduleGraph scheduleGraph = Graphs.scheduleGraph(schedule, job);

    return scheduleGraph.getDependentProcesses(cloudiatorProcess);
  }

  private void notifyBeforeDeletion(Schedule schedule, Job job,
      CloudiatorProcess toBeDeleted) {

    for (CloudiatorProcess dependency : dependencies(schedule, job, toBeDeleted)) {

      final Task runningTask = job.getTask(toBeDeleted.taskId()).orElseThrow(
          () -> new IllegalStateException("Task of running process is not known by job"));

      final TaskInterface toBeDeletedTaskInterface = runningTask
          .interfaceOfName(toBeDeleted.taskInterface());

      final Task toBeNotifiedTask = job.getTask(dependency.taskId())
          .orElseThrow(
              () -> new IllegalStateException("Task of dependency process is not known by job"));

      final TaskInterface toBeNotifiedTaskInterface = toBeNotifiedTask
          .interfaceOfName(dependency.taskInterface());

      if (taskUpdaters.supports(toBeNotifiedTaskInterface)) {

        if (toBeNotifiedTaskInterface.requiresManualWait(toBeDeletedTaskInterface)) {
          taskUpdaters.notifyDelete(schedule, job, toBeNotifiedTaskInterface, toBeNotifiedTask,
              toBeDeleted);
        }
      }
    }
  }

  @Override
  @Transactional
  public void kill(CloudiatorProcess cloudiatorProcess) throws ProcessDeletionException {

    final Schedule schedule = scheduleDomainRepository.findByProcess(cloudiatorProcess);

    if (schedule == null) {
      throw new IllegalStateException(
          String.format("Schedule for process %s no longer exists.", cloudiatorProcess));
    }

    final Job job = jobMessageRepository.getById(cloudiatorProcess.userId(), schedule.job());

    if (job == null) {
      throw new IllegalStateException(
          String.format("Job for schedule %s does not longer exists.", schedule));
    }

    checkState(supports(cloudiatorProcess),
        String.format("%s does not support killing the process %s.", this, cloudiatorProcess));

    checkState(cloudiatorProcess.state().isRemovable(),
        String.format("Process is in state %s and can not be removed.", cloudiatorProcess.state()));

    for (ProcessKiller processKiller : processKillers) {
      if (processKiller.supports(cloudiatorProcess)) {
        notifyBeforeDeletion(schedule, job, cloudiatorProcess);
        processKiller.kill(cloudiatorProcess);
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("processKillers", processKillers).toString();
  }
}
