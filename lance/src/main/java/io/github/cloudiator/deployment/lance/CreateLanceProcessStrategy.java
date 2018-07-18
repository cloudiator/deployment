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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.lance.application.ApplicationId;
import de.uniulm.omi.cloudiator.lance.application.ApplicationInstanceId;
import de.uniulm.omi.cloudiator.lance.application.DeploymentContext;
import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.application.component.DeployableComponent;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import de.uniulm.omi.cloudiator.lance.container.spec.os.OperatingSystem;
import de.uniulm.omi.cloudiator.lance.lca.DeploymentException;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import de.uniulm.omi.cloudiator.lance.lca.registry.RegistrationException;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.LanceProcess;
import io.github.cloudiator.deployment.domain.LanceProcessBuilder;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLanceProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLanceProcessStrategy.class);
  private final LanceInstallationStrategy lanceInstallationStrategy;

  @Inject(optional = true)
  @Named("lance.rmiTimeout")
  private int rmiTimeout = 0;

  @Inject
  CreateLanceProcessStrategy(
      LanceInstallationStrategy lanceInstallationStrategy) {
    this.lanceInstallationStrategy = lanceInstallationStrategy;
  }

  private LifecycleClient getLifecycleClient(String serverIp) {
    final LifecycleClient lifecycleClient;
    try {
      lifecycleClient = LifecycleClient
          .getClient(serverIp, rmiTimeout);
    } catch (RemoteException | NotBoundException e) {
      throw new IllegalStateException("Error creating lifecycle client", e);
    }
    return lifecycleClient;
  }

  private static ContainerType deriveContainerType(LanceInterface lanceInterface, Node node) {

    switch (lanceInterface.containerType()) {
      case DOCKER:
        return ContainerType.DOCKER;
      case NATIVE:
        return ContainerType.PLAIN;
      case BOTH:
        return deriveContainerTypeBasedOnNode(node);
      default:
        throw new AssertionError("Unrecognized container type " + lanceInterface.containerType());
    }
  }

  private static ContainerType deriveContainerTypeBasedOnNode(Node node) {
    final de.uniulm.omi.cloudiator.domain.OperatingSystem operatingSystem = node.nodeProperties()
        .operatingSystem().orElseThrow(() -> new IllegalStateException(String.format(
            "Could not determine container type based on node %s as operating system is not defined.",
            node)));

    if (operatingSystem.operatingSystemFamily().operatingSystemType().supportsDocker()) {
      return ContainerType.DOCKER;
    }
    return ContainerType.PLAIN;
  }


  public LanceProcess execute(String userId, Schedule schedule, Task task, Node node) {

    ContainerType containerType = deriveContainerType(task.interfaceOfType(LanceInterface.class),
        node);

    lanceInstallationStrategy.execute(userId, node, containerType);

    final LifecycleClient lifecycleClient = getLifecycleClient(
        node.connectTo().ip());

    final ApplicationId applicationId = ApplicationId.fromString(schedule.job().id());
    final ApplicationInstanceId applicationInstanceId = ApplicationInstanceId
        .fromString(schedule.id());

    LOGGER.debug(String.format(
        "Registering new applicationInstance %s for application %s at lance using client %s",
        applicationInstanceId, applicationId, lifecycleClient));

    try {
      final boolean couldRegister = lifecycleClient
          .registerApplicationInstance(applicationInstanceId, applicationId);

      //if the instance could still be registered we need to register its subparts
      if (couldRegister) {
        registerApplicationComponentsForApplicationInstance(lifecycleClient,
            applicationInstanceId, schedule.job());
        LOGGER.debug(String.format(
            "Could register applicationInstance %s, therefore registering all components of the job",
            applicationInstanceId));
      } else {
        LOGGER.debug(String.format(
            "Could not register applicationInstance %s, assuming it was already registered.",
            applicationInstanceId));
      }

    } catch (RegistrationException e) {
      throw new IllegalStateException("Could not register application instance", e);
    }

    final DeploymentContext deploymentContext =
        lifecycleClient.initDeploymentContext(applicationId, applicationInstanceId);
    LOGGER.debug(String.format("Initialized deployment context %s.", deploymentContext));

    LOGGER.debug(String
        .format("Creating deployable component for task %s.",
            task));
    final DeployableComponent deployableComponent = new DeployableComponentSupplier(schedule.job(),
        task).get();
    LOGGER.debug(
        String.format("Successfully build deployable component %s", deployableComponent));

    new RegisterTaskDeploymentContextVisitor(schedule.job(), task)
        .visitDeploymentContext(deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, deployableComponent %s, containerType %s.",
        lifecycleClient, deploymentContext, deployableComponent, containerType));

    try {
      ComponentInstanceId componentInstanceId = lifecycleClient
          .deploy(deploymentContext, deployableComponent, OperatingSystem.UBUNTU_14_04,
              containerType);

      lifecycleClient.waitForDeployment(componentInstanceId);

      LOGGER.debug(String
          .format(
              "Client deployed the process of task %s with component instance id %s successfully",
              task, componentInstanceId));

      return LanceProcessBuilder.newBuilder().id(componentInstanceId.toString()).node(node)
          .schedule(schedule).task(task.name()).build();


    } catch (DeploymentException e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }


  }

  private void registerApplicationComponentsForApplicationInstance(
      LifecycleClient lifecycleClient, ApplicationInstanceId applicationInstanceId, Job job) {

    LOGGER.debug(String
        .format("Starting registration of application components for applicationInstance %s.",
            applicationInstanceId));

    for (Task task : job.tasks()) {
      final ComponentId componentId = ComponentId.fromString(job.id() + "/" + task.name());

      try {
        lifecycleClient
            .registerComponentForApplicationInstance(applicationInstanceId, componentId,
                task.name());
        LOGGER.debug(String.format(
            "Registered task %s as component with id %s for applicationInstance %s.",
            task, componentId, applicationInstanceId));
      } catch (RegistrationException e) {
        throw new IllegalStateException("Could not register component for application instance.",
            e);
      }
    }
  }

}
