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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.Schedule.Builder;

public class ScheduleConverter implements TwoWayConverter<ProcessEntities.Schedule, Schedule> {

  public static final ScheduleConverter INSTANCE = new ScheduleConverter();
  public static final ScheduleStateConverter SCHEDULE_STATE_CONVERTER = new ScheduleStateConverter();

  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final InstantiationConverter INSTANTIATION_CONVERTER = InstantiationConverter.INSTANCE;

  private ScheduleConverter() {
  }

  @Override
  public ProcessEntities.Schedule applyBack(Schedule schedule) {

    final Builder builder = ProcessEntities.Schedule.newBuilder();
    builder.setId(schedule.id()).setUserId(schedule.userId())
        .setInstantiation(INSTANTIATION_CONVERTER.applyBack(schedule.instantiation()))
        .setState(SCHEDULE_STATE_CONVERTER.applyBack(schedule.state()))
        .setJob(schedule.job());

    schedule.processes().stream().map(PROCESS_MESSAGE_CONVERTER::applyBack)
        .forEach(builder::addProcesses);

    return builder.build();
  }

  @Override
  public Schedule apply(ProcessEntities.Schedule schedule) {

    final Schedule result = ScheduleImpl
        .of(schedule.getId(), schedule.getUserId(), schedule.getJob(),
            INSTANTIATION_CONVERTER.apply(schedule.getInstantiation()),
            SCHEDULE_STATE_CONVERTER.apply(schedule.getState()));

    schedule.getProcessesList().stream().map(PROCESS_MESSAGE_CONVERTER)
        .forEach(result::addProcess);

    return result;
  }

  private static class ScheduleStateConverter implements
      TwoWayConverter<ProcessEntities.ScheduleState, ScheduleState> {

    @Override
    public ProcessEntities.ScheduleState applyBack(ScheduleState scheduleState) {

      switch (scheduleState) {
        case PENDING:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_PENDING;
        case ERROR:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_ERROR;
        case DELETED:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_DELETED;
        case RUNNING:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_RUNNING;
        case MANUAL:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_MANUAL;
        case RESTORING:
          return ProcessEntities.ScheduleState.SCHEDULE_STATE_RESTORING;
        default:
          throw new AssertionError("Unknown schedule state " + scheduleState);
      }
    }

    @Override
    public ScheduleState apply(ProcessEntities.ScheduleState scheduleState) {
      switch (scheduleState) {
        case SCHEDULE_STATE_ERROR:
          return ScheduleState.ERROR;
        case SCHEDULE_STATE_DELETED:
          return ScheduleState.DELETED;
        case SCHEDULE_STATE_PENDING:
          return ScheduleState.PENDING;
        case SCHEDULE_STATE_RUNNING:
          return ScheduleState.RUNNING;
        case SCHEDULE_STATE_MANUAL:
          return ScheduleState.MANUAL;
        case SCHEDULE_STATE_RESTORING:
          return ScheduleState.RESTORING;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown schedule state " + scheduleState);
      }
    }
  }
}
