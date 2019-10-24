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

package io.github.cloudiator.deployment.scheduler;

import com.google.common.base.MoreObjects;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.deployment.config.DeploymentContext;
import io.github.cloudiator.deployment.config.DeploymentModule;
import io.github.cloudiator.deployment.scheduler.config.SchedulerModule;
import io.github.cloudiator.deployment.scheduler.messaging.DeleteProcessRequestSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.DeleteScheduleRequestSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.NodeEventSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ProcessEventSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ProcessQuerySubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ProcessRequestSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ScaleRequestSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ScheduleEventSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ScheduleGraphSubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ScheduleQuerySubscriber;
import io.github.cloudiator.deployment.scheduler.messaging.ScheduleRequestSubscriber;
import io.github.cloudiator.persistance.DeploymentJpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleAgent {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteScheduleRequestSubscriber.class);

  private final static Injector INJECTOR = Guice
      .createInjector(
          new KafkaMessagingModule(new KafkaContext()), new MessageServiceModule(),
          new SchedulerModule(new DeploymentContext()), new DeploymentModule(new DeploymentContext()),
          new DeploymentJpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())));

  public static void main(String[] args) {

    LOGGER.info(String.format("Starting %s.", ScheduleRequestSubscriber.class));
    INJECTOR.getInstance(ScheduleRequestSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ProcessRequestSubscriber.class));
    INJECTOR.getInstance(ProcessRequestSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ScheduleQuerySubscriber.class));
    INJECTOR.getInstance(ScheduleQuerySubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ProcessQuerySubscriber.class));
    INJECTOR.getInstance(ProcessQuerySubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", DeleteProcessRequestSubscriber.class));
    INJECTOR.getInstance(DeleteProcessRequestSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", DeleteScheduleRequestSubscriber.class));
    INJECTOR.getInstance(DeleteScheduleRequestSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ScheduleGraphSubscriber.class));
    INJECTOR.getInstance(ScheduleGraphSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", NodeEventSubscriber.class));
    INJECTOR.getInstance(NodeEventSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ProcessEventSubscriber.class));
    INJECTOR.getInstance(ProcessEventSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ScheduleEventSubscriber.class));
    INJECTOR.getInstance(ScheduleEventSubscriber.class).run();

    LOGGER.info(String.format("Starting %s.", ScaleRequestSubscriber.class));
    INJECTOR.getInstance(ScaleRequestSubscriber.class).run();


  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
