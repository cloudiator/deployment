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

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
class FixedDistributionModel extends DistributionModel {

  @Column(nullable = false)
  private double value;

  protected FixedDistributionModel() {

  }

  protected FixedDistributionModel(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }

  public FixedDistributionModel setValue(double value) {
    this.value = value;
    return this;
  }
}
