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

import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.Stateful;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.domain.Node;
import java.util.Collection;
import java.util.Set;

public interface Schedule extends Identifiable, Stateful<ScheduleState> {

  enum Instantiation {
    AUTOMATIC,
    MANUAL
  }

  enum ScheduleState implements State {
    PENDING,
    RUNNING,
    ERROR,
    RESTORING,
    DELETED,
    MANUAL
  }

  String job();

  String userId();

  Set<CloudiatorProcess> processes();

  Instantiation instantiation();

  Schedule addProcess(CloudiatorProcess cloudiatorProcess);

  Schedule addProcesses(Collection<? extends CloudiatorProcess> processes);

  @Override
  ScheduleState state();

  Schedule setState(ScheduleState scheduleState);

  boolean runsOnNode(Node node);

  Task getTask(CloudiatorProcess cloudiatorProcess, Job job);

  Set<CloudiatorProcess> processesForTask(Task task);

  Set<CloudiatorProcess> processesForNode(Node node);

  Set<String> nodes();

  boolean hasProcess(CloudiatorProcess cloudiatorProcess);

  void notifyOfProcess(Job job, CloudiatorProcess cloudiatorProcess, TaskUpdater taskUpdater);
}
