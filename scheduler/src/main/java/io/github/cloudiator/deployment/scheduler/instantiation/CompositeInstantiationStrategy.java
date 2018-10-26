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

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeInstantiationStrategy implements InstantiationStrategy {

  private final Set<InstantiationStrategy> strategies;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeInstantiationStrategy.class);

  @Inject
  public CompositeInstantiationStrategy(
      Set<InstantiationStrategy> strategies) {
    this.strategies = strategies;
  }

  @Override
  public boolean supports(Instantiation instantiation) {
    return true;
  }

  @Override
  public void instantiate(Schedule schedule, Job job, String userId) throws InstantiationException {

    for (InstantiationStrategy instantiationStrategy : strategies) {
      if (instantiationStrategy.supports(schedule.instantiation())) {
        LOGGER.info(String.format("Using instantiation strategy %s to instantiate schedule %s.",
            instantiationStrategy, schedule));
        instantiationStrategy.instantiate(schedule, job, userId);
        return;
      }
    }
    throw new InstantiationException(String
        .format("None of the found strategies [%s] support the schedule %s.",
            Joiner.on(",").join(strategies),
            schedule));
  }
}
