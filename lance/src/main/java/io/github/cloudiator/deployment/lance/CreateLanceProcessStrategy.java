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
import de.uniulm.omi.cloudiator.lance.application.component.DockerComponent;
import de.uniulm.omi.cloudiator.lance.application.component.RemoteDockerComponent;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import de.uniulm.omi.cloudiator.lance.container.spec.os.OperatingSystem;
import de.uniulm.omi.cloudiator.lance.lca.DeploymentException;
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import de.uniulm.omi.cloudiator.lance.lca.registry.RegistrationException;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLanceProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLanceProcessStrategy.class);
  private final LanceInstallationStrategy lanceInstallationStrategy;
  private final LanceClientConnector lanceClientConnector;


  @Inject
  CreateLanceProcessStrategy(
      LanceInstallationStrategy lanceInstallationStrategy,
      LanceClientConnector lanceClientConnector) {
    this.lanceInstallationStrategy = lanceInstallationStrategy;
    this.lanceClientConnector = lanceClientConnector;
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


  public CloudiatorSingleProcess execute(String userId, String schedule, Job job, Task task, NodeGroup nodeGroup) {

    if(nodeGroup.getNodes().size()>1){
      throw  new IllegalStateException("NodeGroup for LanceProcess contains more than one node: " + nodeGroup.getNodes().size() + " nodes! This should never happen!");
    }
    Optional<Node> mandatoryNode = nodeGroup.getNodes().stream().findFirst();

    if(!mandatoryNode.isPresent()){
      throw  new IllegalStateException("NodeGroup for LanceProcess does not contain any nodes!");
    }
    Node node = mandatoryNode.get();

    LOGGER.info(String
        .format("Creating new CloudiatorProcess for user: %s, schedule %s, task %s on nodeGroup %s",
            userId, schedule, task, nodeGroup));
    ContainerType containerType = ContainerType.PLAIN;

    if (usesLanceInterface(task)) {
      containerType =
          deriveContainerType(task.interfaceOfType(LanceInterface.class), node);
      LOGGER.debug("Executing lance installation strategy for a process connected with a LanceInterface");
      lanceInstallationStrategy.execute(userId, node, containerType);
    } else if (usesDockerInterface(task)) {
      LOGGER.debug("Executing lance installation strategy for a process connected with a DockerInterface");
      lanceInstallationStrategy.execute(userId, node);
    } else {
      throw new IllegalStateException("Wrong task interface submitted. Must be either LanceInterface or DockerInterface");
    }

    final LifecycleClient lifecycleClient = lanceClientConnector.getLifecycleClient(
        node.connectTo().ip());

    final ApplicationId applicationId = ApplicationId.fromString(job.id());
    final ApplicationInstanceId applicationInstanceId = ApplicationInstanceId
        .fromString(schedule);

    LOGGER.debug(String.format(
        "Registering new applicationInstance %s for application %s at lance using client %s",
        applicationInstanceId, applicationId, lifecycleClient));

    try {
      final boolean couldRegister = lifecycleClient
          .registerApplicationInstance(applicationInstanceId, applicationId);

      //if the instance could still be registered we need to register its subparts
      if (couldRegister) {
        registerApplicationComponentsForApplicationInstance(lifecycleClient,
            applicationInstanceId, job);
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

    if (usesLanceInterface(task)) {
      LOGGER.debug("Deploying a LifecycleComponent for a " + containerType + " container connected with a LanceInterface");
      return deployLifecycleComponent(task, job, deploymentContext, lifecycleClient, containerType, schedule, nodeGroup.id());
    } else if (usesDockerInterface(task)) {
      LOGGER.debug("Deploying a DockerComponent for a container connected with a DockerInterface");
      return deployDockerComponent(task, job, deploymentContext, lifecycleClient, schedule, nodeGroup.id());
    } else {
      throw new IllegalStateException("Wrong task interface submitted. Must be either LanceInterface or DockerInterface");
    }
  }

  private CloudiatorProcess deployLifecycleComponent(Task task, Job job, DeploymentContext deploymentContext,
      LifecycleClient lifecycleClient, ContainerType containerType, String schedule, String nodeGroupId) {
    LOGGER.debug(String
        .format("Creating Lifecycle component for task %s.",
            task));
    final DeployableComponent deployableComponent = new DeployableComponentSupplier(job,
        task).get();
    LOGGER.debug(
        String.format("Successfully build Lifecycle component %s", deployableComponent));

    new RegisterTaskDeploymentContextVisitor(job, task)
        .visitDeploymentContext(deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Lifecycle Component %s, containerType %s.",
        lifecycleClient, deploymentContext, deployableComponent, containerType));

    try {
      ComponentInstanceId componentInstanceId = lifecycleClient
          .deploy(deploymentContext, deployableComponent, OperatingSystem.UBUNTU_14_04,
              containerType);
      return waitForDeployment(lifecycleClient, componentInstanceId, task, schedule, nodeGroupId);
    } catch (DeploymentException e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }
  }

  private CloudiatorProcess deployDockerComponent(Task task, Job job, DeploymentContext deploymentContext,
      LifecycleClient lifecycleClient, String schedule, String nodeGroupId) {
    if(DockerComponentSupplier.usePrivateRegistry(task.interfaceOfType(DockerInterface.class))) {
      return deployPrivateDockerComponent(task, job, deploymentContext, lifecycleClient, schedule, nodeGroupId);
    } else {
      return deployPublicDockerComponent(task, job, deploymentContext, lifecycleClient, schedule, nodeGroupId);
    }
  }

  private CloudiatorProcess deployPrivateDockerComponent(Task task, Job job, DeploymentContext deploymentContext,
    LifecycleClient lifecycleClient, String schedule, String nodeGroupId) {
    LOGGER.debug(String
          .format("Creating Private Docker component for task %s.",
        task));

    final RemoteDockerComponent remoteDockerComponent = new PrivateDockerComponentSupplier(job, task).get();
    LOGGER.debug(
        String.format("Successfully build Private Docker component %s", remoteDockerComponent));

    new RegisterTaskDeploymentContextVisitor(job, task)
        .visitDeploymentContext(deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Private Docker Component %s.",
        lifecycleClient, deploymentContext, remoteDockerComponent));

    try {
      ComponentInstanceId componentInstanceId = lifecycleClient.deploy(deploymentContext, remoteDockerComponent);
      return waitForDeployment(lifecycleClient, componentInstanceId, task, schedule, nodeGroupId);
    } catch (DeploymentException e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }
  }

  private CloudiatorProcess deployPublicDockerComponent(Task task, Job job, DeploymentContext deploymentContext,
      LifecycleClient lifecycleClient, String schedule, String nodeGroupId) {
    LOGGER.debug(String
        .format("Creating Public Docker component for task %s.",
            task));

    final DockerComponent dockerComponent = new PublicDockerComponentSupplier(job, task).get();
    LOGGER.debug(
        String.format("Successfully build Public Docker component %s", dockerComponent));

    new RegisterTaskDeploymentContextVisitor(job, task)
        .visitDeploymentContext(deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Public Docker Component %s.",
        lifecycleClient, deploymentContext, dockerComponent));

    try {
      ComponentInstanceId componentInstanceId = lifecycleClient.deploy(deploymentContext, dockerComponent);
      return waitForDeployment(lifecycleClient, componentInstanceId, task, schedule, nodeGroupId );
    } catch (DeploymentException e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }
  }

  private CloudiatorProcess waitForDeployment(LifecycleClient client, ComponentInstanceId cId, Task task, String schedule, String nodeGroupId) {
    client.waitForDeployment(cId);

    LOGGER.debug(String
        .format(
            "Client deployed the process of task %s with component instance id %s successfully",
            task, cId));

    return CloudiatorProcessBuilder.newBuilder()
        .id(cId.toString())
        .nodeGroup(nodeGroupId)
        .type(Type.LANCE)
        .taskName(task.name()).scheduleId(schedule).build();
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

  private boolean usesLanceInterface(Task task) {
    try {
      task.interfaceOfType(LanceInterface.class);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER
          .debug("Provided task does not contain a LanceInterface. Skip creating a process for a task supporting a LanceInterface...");
      return false;
    }
  }

  private boolean usesDockerInterface(Task task) {
    try {
      task.interfaceOfType(DockerInterface.class);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER
          .debug("Provided task does not contain a DockerInterface. Skip creating a process for a task supporting a DockerInterface...");
      return false;
    }
  }
}
