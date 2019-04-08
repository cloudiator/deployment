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

package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class PeriodicBehaviourImpl implements Periodic {

  private final Interval interval;
  private final CollisionHandling collisionHandling;

  PeriodicBehaviourImpl(Interval interval,
      CollisionHandling collisionHandling) {
    checkNotNull(interval, "interval is null");
    checkNotNull(collisionHandling, "collisionHandling is null");
    this.interval = interval;
    this.collisionHandling = collisionHandling;
  }

  @Override
  public Interval interval() {
    return interval;
  }

  @Override
  public CollisionHandling collisionHandling() {
    return collisionHandling;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("interval", interval)
        .add("collisionHandling", collisionHandling).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicBehaviourImpl that = (PeriodicBehaviourImpl) o;
    return interval.equals(that.interval) &&
        collisionHandling == that.collisionHandling;
  }

  @Override
  public int hashCode() {
    return Objects.hash(interval, collisionHandling);
  }
}
