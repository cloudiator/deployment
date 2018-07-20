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

package io.github.cloudiator.deployment.lance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.application.component.DeployableComponent;
import de.uniulm.omi.cloudiator.lance.application.component.DeployableComponentBuilder;
import de.uniulm.omi.cloudiator.lance.application.component.OutPort;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties.PortType;
import de.uniulm.omi.cloudiator.lance.client.DeploymentHelper;
import de.uniulm.omi.cloudiator.lance.container.spec.os.OperatingSystem;
import de.uniulm.omi.cloudiator.lance.lifecycle.bash.BashBasedHandlerBuilder;
import de.uniulm.omi.cloudiator.lance.lifecycle.detector.PortUpdateHandler;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import java.util.function.Supplier;

public class DeployableComponentSupplier implements Supplier<DeployableComponent> {

  private final Job job;
  private final Task task;
  private static final LanceTaskInterfaceToLifecycleStore LANCE_TASK_INTERFACE_TO_LIFECYCLE_STORE = new LanceTaskInterfaceToLifecycleStore();

  public DeployableComponentSupplier(Job job, Task task) {
    checkNotNull(job, "job is null");
    checkNotNull(task, "task is null");

    checkArgument(job.tasks().contains(task), "Task %s is not member of job %s.", task, job);

    this.job = job;
    this.task = task;
  }

  private LanceInterface lanceInterface() {
    return task.interfaceOfType(LanceInterface.class);
  }

  private PortType derivePortType(PortProvided provided) {
    if (job.attachedCommunications(provided).isEmpty()) {
      return PortProperties.PortType.PUBLIC_PORT;
    } else {
      // todo should be internal, but for the time being we use public here
      // todo facilitates the security group handling
      // todo portType = PortProperties.PortType.INTERNAL_PORT;
      return PortProperties.PortType.PUBLIC_PORT;
    }
  }

  private int deriveMinSinks(PortRequired portRequired) {
    if (portRequired.isMandatory()) {
      return 1;
    }
    return OutPort.NO_SINKS;
  }

  private PortUpdateHandler portUpdateHandler(PortRequired portRequired) {
    if (!portRequired.updateAction().isPresent()) {
      return DeploymentHelper.getEmptyPortUpdateHandler();
    }

    //todo this is inconsistent. multiple PortUpdateHandler should be allowed here so
    //todo it is possible to set one per operating system!
    BashBasedHandlerBuilder portUpdateBuilder = new BashBasedHandlerBuilder();
    portUpdateBuilder.setOperatingSystem(OperatingSystem.UBUNTU_14_04);
    portUpdateBuilder.addCommand(portRequired.updateAction().get());
    return portUpdateBuilder.buildPortUpdateHandler();

  }

  @Override
  public DeployableComponent get() {

    final DeployableComponentBuilder builder = DeployableComponentBuilder
        .createBuilder(task.name(), ComponentId.fromString(job.id() + "/" + task.name()));

    // add all ingoing ports / provided ports
    for (PortProvided provided : task.providedPorts()) {
      builder.addInport(provided.name(), derivePortType(provided),
          PortProperties.INFINITE_CARDINALITY);
    }

    // add all outports / required ports
    for (PortRequired required : task.requiredPorts()) {
      builder.addOutport(required.name(), portUpdateHandler(required),
          PortProperties.INFINITE_CARDINALITY, deriveMinSinks(required));
    }

    builder.addLifecycleStore(LANCE_TASK_INTERFACE_TO_LIFECYCLE_STORE.apply(lanceInterface()));

    return builder.build();
  }
}
