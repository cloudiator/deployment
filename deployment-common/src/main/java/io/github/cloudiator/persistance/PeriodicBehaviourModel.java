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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.domain.CollisionHandling;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

@Entity
class PeriodicBehaviourModel extends BehaviourModel {

  @OneToOne(optional = false)
  private IntervalModel interval;

  @Enumerated(EnumType.STRING)
  private CollisionHandling collisionHandling;

  protected PeriodicBehaviourModel() {
  }

  protected PeriodicBehaviourModel(IntervalModel interval,
      CollisionHandling collisionHandling) {

    checkNotNull(interval, "interval is null");
    checkNotNull(collisionHandling, "collisionHandling is null");
    this.interval = interval;
    this.collisionHandling = collisionHandling;
  }

  public IntervalModel getInterval() {
    return interval;
  }

  public CollisionHandling getCollisionHandling() {
    return collisionHandling;
  }
}
