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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Distribution;
import io.github.cloudiator.deployment.domain.FixedDistribution;
import io.github.cloudiator.deployment.domain.NormalDistribution;
import io.github.cloudiator.deployment.domain.StartTime;

public class StartTimeDomainRepository {

  private final StartTimeModelRepository startTimeModelRepository;

  @Inject
  public StartTimeDomainRepository(
      StartTimeModelRepository startTimeModelRepository) {
    this.startTimeModelRepository = startTimeModelRepository;
  }

  StartTimeModel saveAndGet(StartTime domain) {

    final DistributionModel distributionModel = createDistributionModel(domain.distribution());
    final StartTimeModel startTimeModel = new StartTimeModel(
        createDistributionModel(domain.distribution()), domain.timeUnit());

    startTimeModelRepository.save(startTimeModel);
    return startTimeModel;
  }

  private DistributionModel createDistributionModel(Distribution domain) {

    if (domain instanceof NormalDistribution) {
      return createNormalDistribution((NormalDistribution) domain);
    } else {
      throw new AssertionError("TaskBehaviour is of unknown type " + domain.getClass().getName());
    }
  }

  private DistributionModel createNormalDistribution(NormalDistribution domain) {
    return new NormalDistributionModel(domain.mean(), domain.stdDev());
  }

  private FixedDistributionModel createFixedDistribution(FixedDistribution domain) {
    return new FixedDistributionModel(domain.value());
  }
}
