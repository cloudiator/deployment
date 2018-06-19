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

package io.github.cloudiator.deployment.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortProvidedBuilder;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.PortRequiredBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.PortRequired.Builder;

public class PortConverter implements TwoWayConverter<TaskEntities.Port, Port> {

  @Override
  public TaskEntities.Port applyBack(Port port) {

    if (port instanceof PortRequired) {

      final Builder builder = TaskEntities.PortRequired.newBuilder();
      builder.setName(port.name())
          .setIsMandatory(((PortRequired) port).isMandatory());
      if (((PortRequired) port).updateAction().isPresent()) {
        builder.setUpdateAction(((PortRequired) port).updateAction().get());
      }

      final TaskEntities.PortRequired portRequired = builder.build();
      return TaskEntities.Port.newBuilder().setPortRequired(portRequired).build();
    } else if (port instanceof PortProvided) {
      final TaskEntities.PortProvided portProvided = TaskEntities.PortProvided.newBuilder()
          .setName(port.name()).setPort(((PortProvided) port).port()).build();
      return TaskEntities.Port.newBuilder().setPortProvided(portProvided).build();
    } else {
      throw new AssertionError(String.format("Unknown port class %s.", port.getClass().getName()));
    }
  }

  @Override
  public Port apply(TaskEntities.Port port) {

    switch (port.getPortCase()) {

      case PORTPROVIDED:
        final TaskEntities.PortProvided portProvided = port.getPortProvided();
        return PortProvidedBuilder.newBuilder().name(portProvided.getName())
            .port(portProvided.getPort()).build();
      case PORTREQUIRED:
        final TaskEntities.PortRequired portRequired = port.getPortRequired();

        final PortRequiredBuilder builder = PortRequiredBuilder.newBuilder()
            .name(portRequired.getName())
            .isMandatory(portRequired.getIsMandatory());

        if (!Strings.isNullOrEmpty(portRequired.getUpdateAction())) {
          builder.updateAction(portRequired.getUpdateAction());
        }
        return builder.build();

      case PORT_NOT_SET:
        throw new IllegalStateException("Port not set.");
      default:
        throw new AssertionError(String.format("Unknown port case %s.", port.getPortCase()));
    }
  }
}
