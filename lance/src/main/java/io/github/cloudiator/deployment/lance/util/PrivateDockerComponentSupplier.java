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

package io.github.cloudiator.deployment.lance.util;

import de.uniulm.omi.cloudiator.lance.application.component.DockerComponent;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties;
import de.uniulm.omi.cloudiator.lance.application.component.RemoteDockerComponent;
import de.uniulm.omi.cloudiator.lance.client.DeploymentHelper;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import java.util.function.Supplier;

//For all repos that aren't dockerhub
public class PrivateDockerComponentSupplier extends DockerComponentSupplier implements
    Supplier<RemoteDockerComponent> {

  public PrivateDockerComponentSupplier(Job job, Task task, DockerInterface dockerInterface) {
    super(job, task, dockerInterface);
  }

  @Override
  public RemoteDockerComponent get() {
    DockerComponent.Builder builder = new DockerComponent.Builder(deriveEntireCommands(),
        getActualImageName());
    builder.name(task.name());
    builder.myId(ComponentIdGenerator.generate(job.id(), task.name()));
    builder.imageFolder(getImageNameSpace());
    builder.tag(getTagName());

    // add all ingoing ports / provided ports
    for (PortProvided provided : task.providedPorts()) {
      builder.addInport(provided.name(), ComponentSupplierUtils.derivePortType(job, provided),
          PortProperties.INFINITE_CARDINALITY);
    }

    // add all outports / required ports
    for (PortRequired required : task.requiredPorts()) {
      //todo: Create logic to use a portUpdateHandler
      builder.addOutport(required.name(), DeploymentHelper.getEmptyPortUpdateHandler(),
          PortProperties.INFINITE_CARDINALITY, ComponentSupplierUtils.deriveMinSinks(required));
    }

    RemoteDockerComponent.DockerRegistry dReg = new RemoteDockerComponent.DockerRegistry(
        getHostName(), getPort(), getCredentials().username, getCredentials().password, true);
    RemoteDockerComponent rDockerComp = new RemoteDockerComponent(builder, dReg);

    return rDockerComp;
  }
}
