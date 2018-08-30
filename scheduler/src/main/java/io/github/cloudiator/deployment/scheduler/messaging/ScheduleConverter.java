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

package io.github.cloudiator.deployment.scheduler.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.Schedule.Builder;

public class ScheduleConverter implements OneWayConverter<Schedule, ProcessEntities.Schedule> {

  public static final ScheduleConverter INSTANCE = new ScheduleConverter();
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;

  private ScheduleConverter() {}

  @Override
  public ProcessEntities.Schedule apply(Schedule schedule) {

    final Builder builder = ProcessEntities.Schedule.newBuilder().setId(schedule.id())
        .setJob(schedule.job())
        .setInstantiaton(schedule.instantiation().toMessage());

    schedule.processes().stream().map(PROCESS_MESSAGE_CONVERTER::applyBack)
        .forEach(builder::addProcesses);

    return builder.build();
  }
}
