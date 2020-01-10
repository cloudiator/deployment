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

public class NormalDistributionImpl implements NormalDistribution {

  private final double mean;
  private final double stdDev;

  public NormalDistributionImpl(double mean, double stdDev) {
    this.mean = mean;
    this.stdDev = stdDev;
  }

  @Override
  public double mean() {
    return mean;
  }

  @Override
  public double stdDev() {
    return stdDev;
  }

  @Override
  public double next() {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
