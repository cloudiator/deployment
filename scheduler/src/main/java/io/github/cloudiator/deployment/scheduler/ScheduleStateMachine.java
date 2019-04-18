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

package io.github.cloudiator.deployment.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationException;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategySelector;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.concurrent.ExecutionException;

@Singleton
public class ScheduleStateMachine implements ErrorAwareStateMachine<Schedule, ScheduleState> {

  private final ErrorAwareStateMachine<Schedule, ScheduleState> stateMachine;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final InstantiationStrategySelector instantiationStrategySelector;

  @Inject
  public ScheduleStateMachine(
      ScheduleDomainRepository scheduleDomainRepository,
      InstantiationStrategySelector instantiationStrategySelector) {
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.instantiationStrategySelector = instantiationStrategySelector;
    //noinspection unchecked
    stateMachine = StateMachineBuilder.<Schedule, ScheduleState>builder()
        .errorTransition(error())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.PENDING)
                .to(ScheduleState.RUNNING)
                .action(pendingToRunning())
                .build())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.PENDING)
                .to(ScheduleState.MANUAL)
                .action(manual())
                .build())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.RUNNING)
                .to(ScheduleState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.ERROR)
                .to(ScheduleState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.ERROR)
                .to(ScheduleState.RESTORING)
                .action(errorToRestore())
                .build())
        .addTransition(
            Transitions.<Schedule, ScheduleState>transitionBuilder()
                .from(ScheduleState.RESTORING)
                .to(ScheduleState.RUNNING)
                .action(restoreToRunning())
                .build())
        .build();
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule save(Schedule schedule) {
    scheduleDomainRepository.save(schedule);
    return schedule;
  }

  private ErrorTransition<Schedule, ScheduleState> error() {

    return Transitions.<Schedule, ScheduleState>errorTransitionBuilder()
        .action((o, arguments, throwable) -> save(o.setState(ScheduleState.ERROR)))
        .errorState(ScheduleState.ERROR).build();
  }

  private TransitionAction<Schedule> manual() {
    return new TransitionAction<Schedule>() {
      @Override
      public Schedule apply(Schedule o, Object[] objects) throws ExecutionException {

        if (!o.instantiation().equals(Instantiation.MANUAL)) {
          throw new IllegalStateException("Expected schedule to have manual instantiation");
        }

        try {
          return save(instantiationStrategySelector.get(Instantiation.MANUAL).instantiate(o));
        } catch (InstantiationException e) {
          throw new ExecutionException(
              "Exception while instantiating the schedule: " + e.getMessage(),
              e);
        }
      }
    };
  }

  private TransitionAction<Schedule> pendingToRunning() {
    return new TransitionAction<Schedule>() {
      @Override
      public Schedule apply(Schedule o, Object[] objects) throws ExecutionException {

        if (!o.instantiation().equals(Instantiation.AUTOMATIC)) {
          throw new IllegalStateException("Expected schedule to have automatic instantiation");
        }

        try {
          return save(instantiationStrategySelector.get(Instantiation.AUTOMATIC).instantiate(o));
        } catch (InstantiationException e) {
          throw new ExecutionException(
              "Exception while instantiating the schedule: " + e.getMessage(),
              e);
        }
      }
    };
  }

  private TransitionAction<Schedule> errorToRestore() {
    return new TransitionAction<Schedule>() {
      @Override
      public Schedule apply(Schedule o, Object[] objects) throws ExecutionException {
        throw new UnsupportedOperationException("Restoring is currently not supported");
      }
    };
  }

  private TransitionAction<Schedule> restoreToRunning() {
    return new TransitionAction<Schedule>() {
      @Override
      public Schedule apply(Schedule o, Object[] objects) throws ExecutionException {
        throw new UnsupportedOperationException("Restoring is currently not supported");
      }
    };
  }

  private TransitionAction<Schedule> delete() {
    return new TransitionAction<Schedule>() {
      @Override
      public Schedule apply(Schedule o, Object[] objects) throws ExecutionException {
        throw new UnsupportedOperationException("Deleting is currently not supported");
      }
    };
  }


  @Override
  public Schedule fail(Schedule schedule, Object[] objects, Throwable throwable) {
    return stateMachine.fail(schedule, objects, throwable);
  }

  @Override
  public Schedule apply(Schedule schedule, ScheduleState scheduleState, Object[] objects) {
    return stateMachine.apply(schedule, scheduleState, objects);
  }
}
