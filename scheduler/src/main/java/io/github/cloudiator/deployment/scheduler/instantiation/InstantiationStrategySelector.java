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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Singleton
public class InstantiationStrategySelector {

  private final Map<Instantiation, InstantiationStrategy> strategies;

  @Inject
  public InstantiationStrategySelector(
      Set<InstantiationStrategy> strategies) {

    this.strategies = new HashMap<>();
    for (InstantiationStrategy instantiationStrategy : strategies) {
      this.strategies.put(instantiationStrategy.supports(), instantiationStrategy);
    }
  }

  public InstantiationStrategy get(Instantiation instantiation) {
    final InstantiationStrategy instantiationStrategy = strategies.get(instantiation);
    if (instantiationStrategy == null) {

      throw new NoSuchElementException(
          String.format("No instantiation strategy exists for instantiation %s. Existing"
              + "strategies are for %s", instantiation, strategies.keySet()));
    }
    return instantiationStrategy;
  }
}
