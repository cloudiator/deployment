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
import io.github.cloudiator.deployment.domain.Behaviour;
import io.github.cloudiator.deployment.domain.Behaviours;
import io.github.cloudiator.deployment.domain.Interval;
import io.github.cloudiator.deployment.domain.Intervals;
import javax.annotation.Nullable;

class BehaviourModelConverter implements OneWayConverter<BehaviourModel, Behaviour> {

  public static BehaviourModelConverter INSTANCE = new BehaviourModelConverter();

  private BehaviourModelConverter() {

  }

  @Nullable
  @Override
  public Behaviour apply(@Nullable BehaviourModel behaviourModel) {

    if (behaviourModel == null) {
      return null;
    }

    if (behaviourModel instanceof ServiceBehaviourModel) {
      return convertService((ServiceBehaviourModel) behaviourModel);
    } else if (behaviourModel instanceof PeriodicBehaviourModel) {
      return convertPeriodic((PeriodicBehaviourModel) behaviourModel);
    } else {
      throw new AssertionError(
          "Unknown behaviour model type " + behaviourModel.getClass().getName());
    }
  }

  private Behaviour convertService(ServiceBehaviourModel serviceBehaviourModel) {
    return Behaviours.service(serviceBehaviourModel.isRestart());
  }

  private Behaviour convertPeriodic(PeriodicBehaviourModel periodicBehaviourModel) {

    final Interval interval = Intervals.interval(periodicBehaviourModel.getInterval().getTimeUnit(),
        periodicBehaviourModel.getInterval().getPeriod());

    return Behaviours.periodic(interval, periodicBehaviourModel.getCollisionHandling());
  }


}
