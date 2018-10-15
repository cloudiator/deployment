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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import javax.annotation.Nullable;

class ScheduleModelConverter implements OneWayConverter<ScheduleModel, Schedule> {


  static final ScheduleModelConverter INSTANCE = new ScheduleModelConverter();

  private ScheduleModelConverter() {
  }

  @Nullable
  @Override
  public Schedule apply(@Nullable ScheduleModel scheduleModel) {

    if (scheduleModel == null) {
      return null;
    }

    return ScheduleImpl
        .create(scheduleModel.domainId(), scheduleModel.jobId(), scheduleModel.instantiation());
  }
}
