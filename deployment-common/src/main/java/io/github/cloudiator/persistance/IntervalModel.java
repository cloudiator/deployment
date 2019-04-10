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

import java.util.concurrent.TimeUnit;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
class IntervalModel extends Model {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TimeUnit timeUnit;
  
  private long period;

  protected IntervalModel() {
  }

  protected IntervalModel(TimeUnit timeUnit, long period) {

    checkNotNull(timeUnit, "timeUnit is null");

    this.timeUnit = timeUnit;
    this.period = period;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public long getPeriod() {
    return period;
  }
}
