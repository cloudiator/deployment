/*
 * Copyright 2014-2020 University of Ulm
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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

@Entity
public class StartTimeModel extends Model {

  @OneToOne(cascade = CascadeType.ALL)
  private DistributionModel distributionModel;

  @Enumerated(EnumType.STRING)
  private TimeUnit timeUnit;

  protected StartTimeModel() {
  }

  public StartTimeModel(DistributionModel distributionModel, TimeUnit timeUnit) {
    checkNotNull(distributionModel, "distributionModel is null");
    checkNotNull(timeUnit, "timeUnit is null");
    this.distributionModel = distributionModel;
    this.timeUnit = timeUnit;
  }

  public DistributionModel getDistributionModel() {
    return distributionModel;
  }

  public StartTimeModel setDistributionModel(
      DistributionModel distributionModel) {
    this.distributionModel = distributionModel;
    return this;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public StartTimeModel setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }
}
