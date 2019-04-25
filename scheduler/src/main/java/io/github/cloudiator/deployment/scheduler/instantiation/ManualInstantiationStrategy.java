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

import com.google.common.util.concurrent.ListenableFuture;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.util.List;

public class ManualInstantiationStrategy implements InstantiationStrategy {


  @Override
  public Instantiation supports() {
    return Instantiation.MANUAL;
  }

  @Override
  public WaitLock deployTask(Task task, Schedule schedule,
      List<ListenableFuture<Node>> allocatedResources) {
    throw new UnsupportedOperationException(
        String.format("%s does not support scheduling a single task.", this));
  }

  @Override
  public Schedule instantiate(Schedule schedule) {

    checkState(supports().equals(schedule.instantiation()),
        String.format("%s does not support instantiation %s.", this, schedule.instantiation()));

    return schedule.setState(ScheduleState.MANUAL);

  }
}
