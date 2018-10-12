/*
 * Copyright 2017 University of Ulm
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

package io.github.cloudiator.deployment.jobagent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.deployment.jobagent.messaging.JobAddedSubscriber;
import io.github.cloudiator.deployment.jobagent.messaging.JobGetSubscriber;
import io.github.cloudiator.persistance.DeploymentJpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class JobAgent {

  private final static Injector injector = Guice
      .createInjector(
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())),
          new MessageServiceModule(), new JobAgentModule(),
          new DeploymentJpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())));

  public static void main(String[] args) {

    injector.getInstance(JobAddedSubscriber.class).run();
    injector.getInstance(JobGetSubscriber.class).run();

  }

}
