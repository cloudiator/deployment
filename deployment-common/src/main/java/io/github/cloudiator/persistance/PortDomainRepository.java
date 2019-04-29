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

package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;

public class PortDomainRepository {

  private final PortModelRepository portModelRepository;

  @Inject
  public PortDomainRepository(
      PortModelRepository portModelRepository) {
    this.portModelRepository = portModelRepository;
  }

  void save(Port port, TaskModel task) {
    saveAndGet(port, task);
  }

  PortModel saveAndGet(Port port, TaskModel task) {
    final PortModel model = createModel(port, task);
    portModelRepository.save(model);
    return model;
  }

  private PortModel createModel(Port port, TaskModel task) {

    if (port instanceof PortRequired) {
      return createPortRequiredModel((PortRequired) port, task);
    } else if (port instanceof PortProvided) {
      return createPortProvidedModel((PortProvided) port, task);
    } else {
      throw new AssertionError(
          String.format("Port type %s is not known.", port.getClass().getName()));
    }
  }

  private PortRequiredModel createPortRequiredModel(PortRequired portRequired,
      TaskModel taskModel) {
    return new PortRequiredModel(portRequired.name(), taskModel, portRequired.isMandatory());
  }

  private PortProvidedModel createPortProvidedModel(PortProvided portProvided,
      TaskModel taskModel) {
    return new PortProvidedModel(portProvided.name(), taskModel,
        portProvided.port());
  }


}
