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
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.deployment.config.Constants;
import io.github.cloudiator.deployment.config.DeploymentContext;
import io.github.cloudiator.deployment.domain.LanceTaskUpdater;
import io.github.cloudiator.deployment.domain.TaskUpdater;
import io.github.cloudiator.deployment.scheduler.Init;
import io.github.cloudiator.deployment.scheduler.failure.FailureHandler;
import io.github.cloudiator.deployment.scheduler.failure.NodeFailureReportingInterface;
import io.github.cloudiator.deployment.scheduler.failure.ProcessFailureReportingInterface;
import io.github.cloudiator.deployment.scheduler.failure.ScheduleEventReportingInterface;
import io.github.cloudiator.deployment.scheduler.instantiation.AutomaticInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.InstantiationStrategySelector;
import io.github.cloudiator.deployment.scheduler.instantiation.ManualInstantiationStrategy;
import io.github.cloudiator.deployment.scheduler.instantiation.OnDemandResourcePool;
import io.github.cloudiator.deployment.scheduler.instantiation.PeriodicBehaviourSchedulableFactory;
import io.github.cloudiator.deployment.scheduler.instantiation.ResourcePool;
import io.github.cloudiator.deployment.scheduler.instantiation.TaskUpdaters;
import io.github.cloudiator.deployment.scheduler.processes.CompositeProcessKiller;
import io.github.cloudiator.deployment.scheduler.processes.CompositeProcessSpawnerImpl;
import io.github.cloudiator.deployment.scheduler.processes.FaaSProcessKiller;
import io.github.cloudiator.deployment.scheduler.processes.FaasProcessSpawnerImpl;
import io.github.cloudiator.deployment.scheduler.processes.LanceProcessKillerImpl;
import io.github.cloudiator.deployment.scheduler.processes.LanceProcessSpawnerImpl;
import io.github.cloudiator.deployment.scheduler.processes.ProcessKiller;
import io.github.cloudiator.deployment.scheduler.processes.ProcessSpawner;
import io.github.cloudiator.deployment.scheduler.processes.ProcessStatusChecker;
import io.github.cloudiator.deployment.scheduler.processes.ProcessStatusCheckerImpl;
import io.github.cloudiator.deployment.scheduler.processes.ProcessWatchdog;
import io.github.cloudiator.deployment.scheduler.processes.SimulationProcessKiller;
import io.github.cloudiator.deployment.scheduler.processes.SimulationProcessSpawner;
import io.github.cloudiator.deployment.scheduler.processes.SparkProcessKillerImpl;
import io.github.cloudiator.deployment.scheduler.processes.SparkProcessSpawnerImpl;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerModule extends AbstractModule {

  private final DeploymentContext deploymentContext;
  private final SchedulerContext schedulerContext;

  public SchedulerModule(DeploymentContext deploymentContext,
      SchedulerContext schedulerContext) {
    this.deploymentContext = deploymentContext;
    this.schedulerContext = schedulerContext;
  }

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SchedulerModule.class);

  @Override
  protected void configure() {

    bind(ResourcePool.class).to(OnDemandResourcePool.class);
    bind(Init.class).asEagerSingleton();

    bind(NodeFailureReportingInterface.class).to(FailureHandler.class);
    bind(ProcessFailureReportingInterface.class).to(FailureHandler.class);
    bind(ScheduleEventReportingInterface.class).to(FailureHandler.class);

    install(new FactoryModuleBuilder().build(PeriodicBehaviourSchedulableFactory.class));

    Multibinder<Schedulable> schedulableMultibinder = Multibinder
        .newSetBinder(binder(), Schedulable.class);

    if (schedulerContext.isProcessWatchdogEnabled()) {
      LOGGER.info("Enabling process watchdog.");
      schedulableMultibinder.addBinding().to(ProcessWatchdog.class);
    }

    bindConstant().annotatedWith(Names.named(Constants.API)).to(schedulerContext.api());


    final ScheduledThreadPoolExecutorExecutionService scheduledThreadPoolExecutorExecutionService = new ScheduledThreadPoolExecutorExecutionService(
        new LoggingScheduledThreadPoolExecutor(5));

    scheduledThreadPoolExecutorExecutionService.delayShutdownHook(5, TimeUnit.MINUTES);

    bind(ExecutionService.class).annotatedWith(Names.named("SchedulableExecution"))
        .toInstance(scheduledThreadPoolExecutorExecutionService);

    Multibinder<InstantiationStrategy> instantiationStrategyMultibinder = Multibinder
        .newSetBinder(binder(), InstantiationStrategy.class);
    instantiationStrategyMultibinder.addBinding().to(AutomaticInstantiationStrategy.class);
    instantiationStrategyMultibinder.addBinding().to(ManualInstantiationStrategy.class);

    bind(InstantiationStrategySelector.class);

    bind(ProcessStatusChecker.class).to(ProcessStatusCheckerImpl.class);

    //multi binder for process spawners
    Multibinder<ProcessSpawner> processSpawnerMultibinder = Multibinder
        .newSetBinder(binder(), ProcessSpawner.class);
    processSpawnerMultibinder.addBinding().to(LanceProcessSpawnerImpl.class);
    processSpawnerMultibinder.addBinding().to(SparkProcessSpawnerImpl.class);
    processSpawnerMultibinder.addBinding().to(FaasProcessSpawnerImpl.class);
    processSpawnerMultibinder.addBinding().to(SimulationProcessSpawner.class);
    bind(ProcessSpawner.class).to(CompositeProcessSpawnerImpl.class);

    //multi binder for process killers
    Multibinder<ProcessKiller> processKillerMultibinder = Multibinder
        .newSetBinder(binder(), ProcessKiller.class);
    processKillerMultibinder.addBinding().to(LanceProcessKillerImpl.class);
    processKillerMultibinder.addBinding().to(SparkProcessKillerImpl.class);
    processKillerMultibinder.addBinding().to(SimulationProcessKiller.class);
    processKillerMultibinder.addBinding().to(FaaSProcessKiller.class);
    bind(ProcessKiller.class).to(CompositeProcessKiller.class);

    bind(TaskUpdater.class).to(TaskUpdaters.class);
    Multibinder<TaskUpdater> taskUpdaterMultibinder = Multibinder
        .newSetBinder(binder(), TaskUpdater.class);
    taskUpdaterMultibinder.addBinding().to(LanceTaskUpdater.class);

  }
}
