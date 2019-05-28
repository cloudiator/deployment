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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Distribution;
import io.github.cloudiator.deployment.domain.NormalDistributionImpl;
import javax.annotation.Nullable;

public class DistributionModelConverter implements
    OneWayConverter<DistributionModel, Distribution> {

  @Nullable
  @Override
  public Distribution apply(@Nullable DistributionModel distributionModel) {
    if (distributionModel instanceof NormalDistributionModel) {
      return new NormalDistributionImpl(((NormalDistributionModel) distributionModel).getMean(),
          ((NormalDistributionModel) distributionModel).getStdDev());
    } else {
      throw new AssertionError(
          "Illegal distributionModel type " + distributionModel.getClass().getName());
    }
  }
}
