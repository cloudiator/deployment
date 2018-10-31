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

package io.github.cloudiator.deployment.yaml;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.cloudiator.deployment.yaml.messaging.YAMLRequestSubscriber;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class YAMLAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(
          new MessageServiceModule(), new KafkaMessagingModule(new KafkaContext()));

  public static void main(String[] args) {
    INJECTOR.getInstance(YAMLRequestSubscriber.class).run();
  }

}
