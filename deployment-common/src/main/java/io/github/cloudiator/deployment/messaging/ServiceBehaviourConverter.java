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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Behaviours;
import io.github.cloudiator.deployment.domain.ServiceBehaviour;
import org.cloudiator.messages.entities.TaskEntities;

public class ServiceBehaviourConverter implements
    TwoWayConverter<TaskEntities.ServiceBehaviour, ServiceBehaviour> {

  public static final ServiceBehaviourConverter INSTANCE = new ServiceBehaviourConverter();

  private ServiceBehaviourConverter() {}

  @Override
  public TaskEntities.ServiceBehaviour applyBack(ServiceBehaviour serviceBehaviour) {

    return TaskEntities.ServiceBehaviour.newBuilder().setRestart(serviceBehaviour.restart())
        .build();

  }

  @Override
  public ServiceBehaviour apply(TaskEntities.ServiceBehaviour serviceBehaviour) {
    return (ServiceBehaviour) Behaviours.service(serviceBehaviour.getRestart());
  }
}
