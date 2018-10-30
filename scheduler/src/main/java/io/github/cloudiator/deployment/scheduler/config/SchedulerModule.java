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

package io.github.cloudiator.deployment.scheduler.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.github.cloudiator.deployment.scheduler.Init;
import io.github.cloudiator.deployment.scheduler.OnDemandResourcePool;
import io.github.cloudiator.deployment.scheduler.ResourcePool;
import io.github.cloudiator.deployment.scheduler.instantiation.AutomaticInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.CompositeInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.ManualInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.*;

public class SchedulerModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ProcessSpawner> processSpawnerMultibinder =
        Multibinder.newSetBinder(binder(), ProcessSpawner.class);
    processSpawnerMultibinder.addBinding().to(LanceProcessSpawnerImpl.class);
    processSpawnerMultibinder.addBinding().to(FaasProcessSpawner.class);
    bind(ResourcePool.class).to(OnDemandResourcePool.class);
    bind(Init.class).asEagerSingleton();

    Multibinder<InstantiationStrategy> instantiationStrategyMultibinder = Multibinder
        .newSetBinder(binder(), InstantiationStrategy.class);
    instantiationStrategyMultibinder.addBinding().to(AutomaticInstantiationStrategy.class);
    instantiationStrategyMultibinder.addBinding().to(ManualInstantiationStrategy.class);

    bind(InstantiationStrategy.class).to(CompositeInstantiationStrategy.class);
  }
}
