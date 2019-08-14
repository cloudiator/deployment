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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class IntervalImpl implements Interval {

  private final TimeUnit timeUnit;
  private final long period;

  IntervalImpl(TimeUnit timeUnit, long period) {
    checkNotNull(timeUnit, "timeUnit is null");
    checkArgument(period >= 0, "period needs to be >= 0");
    this.timeUnit = timeUnit;
    this.period = period;
  }

  @Override
  public TimeUnit timeUnit() {
    return timeUnit;
  }

  @Override
  public long period() {
    return period;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("timeUnit", timeUnit).add("period", period)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntervalImpl interval = (IntervalImpl) o;
    return period == interval.period &&
        timeUnit == interval.timeUnit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeUnit, period);
  }
}
