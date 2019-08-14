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
import io.github.cloudiator.deployment.domain.NormalDistribution;
import io.github.cloudiator.deployment.domain.NormalDistributionImpl;
import org.cloudiator.messages.entities.TaskEntities;

public class DistributionConverter implements
    TwoWayConverter<TaskEntities.Distribution, Distribution> {

  public static final DistributionConverter INSTANCE = new DistributionConverter();

  @Override
  public TaskEntities.Distribution applyBack(Distribution distribution) {
    if (distribution instanceof NormalDistribution) {
      return TaskEntities.Distribution.newBuilder().setNormalDistribution(
          TaskEntities.NormalDistribution.newBuilder()
              .setMean(((NormalDistribution) distribution).mean())
              .setStdDev(((NormalDistribution) distribution).stdDev()).build()).build();
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
      case DISTRIBUTION_NOT_SET:
      default:
        throw new AssertionError("Illegal distribution case " + distribution);
    }
  }
}
