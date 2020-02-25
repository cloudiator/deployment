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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Distribution;
import io.github.cloudiator.deployment.domain.FixedDistribution;
import io.github.cloudiator.deployment.domain.FixedDistributionImpl;
import io.github.cloudiator.deployment.domain.NormalDistribution;
import io.github.cloudiator.deployment.domain.NormalDistributionImpl;
import io.github.cloudiator.deployment.domain.StartTime;
import io.github.cloudiator.deployment.domain.StartTimeImpl;
import java.util.concurrent.TimeUnit;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.Unit;
import org.cloudiator.messages.entities.TaskEntities;

public class StartTimeConverter implements
    TwoWayConverter<TaskEntities.StartTime, StartTime> {

  public static final StartTimeConverter INSTANCE = new StartTimeConverter();
  private static final DistributionConverter DISTRIBUTION_CONVERTER = new DistributionConverter();
  private static final TimeUnitConverter TIME_UNIT_CONVERTER = new TimeUnitConverter();

  @Override
  public TaskEntities.StartTime applyBack(StartTime startTime) {
    return TaskEntities.StartTime.newBuilder()
        .setDistribution(DISTRIBUTION_CONVERTER.applyBack(startTime.distribution()))
        .setTimeUnit(TIME_UNIT_CONVERTER.applyBack(startTime.timeUnit())).build();
  }

  @Override
  public StartTime apply(TaskEntities.StartTime startTime) {
    return new StartTimeImpl(DISTRIBUTION_CONVERTER.apply(startTime.getDistribution()),
        TIME_UNIT_CONVERTER.apply(startTime.getTimeUnit()));
  }

  private static class DistributionConverter implements
      TwoWayConverter<TaskEntities.Distribution, Distribution> {

    @Override
    public TaskEntities.Distribution applyBack(Distribution distribution) {
      if (distribution instanceof NormalDistribution) {
        return TaskEntities.Distribution.newBuilder().setNormalDistribution(
            TaskEntities.NormalDistribution.newBuilder()
                .setMean(((NormalDistribution) distribution).mean())
                .setStdDev(((NormalDistribution) distribution).stdDev()).build()).build();
      } else if (distribution instanceof FixedDistribution) {
        return TaskEntities.Distribution.newBuilder().setFixedDistribution(
            TaskEntities.FixedDistribution.newBuilder()
                .setValue(((FixedDistribution) distribution).value()).build()).build();
      } else {
        throw new AssertionError("Unknown distribution type " + distribution.getClass().getName());
      }
    }

    @Override
    public Distribution apply(TaskEntities.Distribution distribution) {
      switch (distribution.getDistributionCase()) {
        case NORMALDISTRIBUTION:
          return new NormalDistributionImpl(distribution.getNormalDistribution().getMean(),
              distribution.getNormalDistribution().getStdDev());
        case FIXEDDISTRIBUTION:
          return new FixedDistributionImpl(distribution.getFixedDistribution().getValue());
        case DISTRIBUTION_NOT_SET:
        default:
          throw new AssertionError("Illegal distribution case " + distribution);
      }
    }
  }


  private static class TimeUnitConverter implements TwoWayConverter<CommonEntities.Unit, TimeUnit> {

    @Override
    public Unit applyBack(TimeUnit unit) {
      return Unit.valueOf(unit.name());
    }

    @Override
    public TimeUnit apply(Unit unit) {
      return TimeUnit.valueOf(unit.name());
    }
  }
}
