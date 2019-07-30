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

import de.uniulm.omi.cloudiator.lance.application.DeploymentContext;
import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.application.component.InPort;
import de.uniulm.omi.cloudiator.lance.application.component.OutPort;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties.PortLinkage;
import de.uniulm.omi.cloudiator.lance.application.component.PortReference;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import java.util.function.Predicate;

public class RegisterTaskDeploymentContextVisitor {

  private final Job job;
  private final Task task;

  public RegisterTaskDeploymentContextVisitor(Job job, Task task) {

    checkNotNull(job, "job is null");
    checkNotNull(task, "task is null");

    checkArgument(job.tasks().contains(task),
        String.format("Task %s is not member of job %s.", task, job));

    this.job = job;
    this.task = task;
  }

  public DeploymentContext visitDeploymentContext(DeploymentContext deploymentContext) {

    for (PortProvided provided : task.providedPorts()) {
      deploymentContext.setProperty(provided.name(), provided.port(), InPort.class);
    }

    for (PortRequired portRequired : task.requiredPorts()) {

      Communication communication = job.communications().stream().filter(
          new Predicate<Communication>() {
            @Override
            public boolean test(Communication communication) {
              return communication.portRequired().equals(portRequired.name());
            }
          }).collect(de.uniulm.omi.cloudiator.util.StreamUtil.getOnly()).orElseThrow(
          () -> new IllegalStateException(String
              .format("Required port with name %s is not mapped to any communication",
                  portRequired.name())));

      final PortProvided providingPort = job.getProvidingPort(portRequired);

      final ComponentId providingComponent = ComponentId
          .fromString(job.id() + "/" + job.providingTask(communication).name());

      deploymentContext.setProperty(portRequired.name(),
          new PortReference(providingComponent, providingPort.name(),
              PortLinkage.ALL), OutPort.class);

    }
    return deploymentContext;
  }


}
