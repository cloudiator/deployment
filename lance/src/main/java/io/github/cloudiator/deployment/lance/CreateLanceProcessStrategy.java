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
import de.uniulm.omi.cloudiator.lance.lca.container.ComponentInstanceId;
import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import de.uniulm.omi.cloudiator.lance.lca.registry.RegistrationException;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcessBuilder;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.domain.Node;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
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


  public CloudiatorProcess execute(String userId, String schedule, Job job, Task task, Node node,
      TaskInterface taskInterface) {

    LOGGER.info(String
        .format("Creating new CloudiatorProcess for user: %s, schedule %s, task %s on node %s",
            userId, schedule, task, node));

    if (taskInterface instanceof LanceInterface) {
      ContainerType containerType =
          deriveContainerType(task.interfaceOfType(LanceInterface.class), node);
      LOGGER.debug(
          "Executing lance installation strategy for a process connected with a LanceInterface");
      lanceInstallationStrategy.execute(userId, node, containerType);
    } else if (taskInterface instanceof DockerInterface) {
      LOGGER.debug(
          "Executing lance installation strategy for a process connected with a DockerInterface");
      lanceInstallationStrategy.execute(userId, node);
    } else {
      throw new AssertionError("Unknown task interface type " + taskInterface.getClass().getName());
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

    DeploymentInfo deplInfo = new DeploymentInfo(task, job, deploymentContext, schedule, node,
        userId, taskInterface);

    if (taskInterface instanceof LanceInterface) {
      ContainerType containerType =
          deriveContainerType(task.interfaceOfType(LanceInterface.class), node);
      LOGGER.debug("Deploying a LifecycleComponent for a " + containerType
          + " container connected with a LanceInterface");
      return deployLifecycleComponent(deplInfo, lifecycleClient, containerType);
    } else {
      LOGGER.debug("Deploying a DockerComponent for a container connected with a DockerInterface");
      return deployDockerComponent(deplInfo, lifecycleClient);
    }
  }

  private CloudiatorProcess deployLifecycleComponent(DeploymentInfo deployInfo,
      LifecycleClient lifecycleClient, ContainerType containerType) {
    LOGGER.debug(String
        .format("Creating Lifecycle component for task %s.",
            deployInfo.task));
    final DeployableComponent deployableComponent = new DeployableComponentSupplier(deployInfo.job,
        deployInfo.task).get();
    LOGGER.debug(
        String.format("Successfully build Lifecycle component %s", deployableComponent));

    new RegisterTaskDeploymentContextVisitor(deployInfo.job, deployInfo.task)
        .visitDeploymentContext(deployInfo.deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Lifecycle Component %s, containerType %s.",
        lifecycleClient, deployInfo.deploymentContext, deployableComponent, containerType));

    return callToDeploy(lifecycleClient, deployInfo, new Callable<ComponentInstanceId>() {
      @Override
      public ComponentInstanceId call() throws Exception {
        return lifecycleClient
            .deploy(deployInfo.deploymentContext, deployableComponent, OperatingSystem.UBUNTU_14_04,
                containerType);
      }
    });
  }

  private CloudiatorProcess deployDockerComponent(DeploymentInfo deployInfo,
      LifecycleClient lifecycleClient) {
    if (DockerComponentSupplier
        .usePrivateRegistry(deployInfo.task.interfaceOfType(DockerInterface.class))) {
      return deployPrivateDockerComponent(deployInfo, lifecycleClient);
    } else {
      return deployPublicDockerComponent(deployInfo, lifecycleClient);
    }
  }

  private CloudiatorProcess deployPrivateDockerComponent(DeploymentInfo deployInfo,
      LifecycleClient lifecycleClient) {
    LOGGER.debug(String
        .format("Creating Private Docker component for task %s.",
            deployInfo.task));

    final RemoteDockerComponent remoteDockerComponent = new PrivateDockerComponentSupplier(
        deployInfo.job, deployInfo.task).get();
    LOGGER.debug(
        String.format("Successfully build Private Docker component %s", remoteDockerComponent));

    new RegisterTaskDeploymentContextVisitor(deployInfo.job, deployInfo.task)
        .visitDeploymentContext(deployInfo.deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Private Docker Component %s.",
        lifecycleClient, deployInfo.deploymentContext, remoteDockerComponent));

    return this.callToDeploy(lifecycleClient, deployInfo, new Callable<ComponentInstanceId>() {
      @Override
      public ComponentInstanceId call() throws Exception {
        return lifecycleClient
            .deploy(deployInfo.deploymentContext, remoteDockerComponent);
      }
    });
  }

  private CloudiatorProcess callToDeploy(LifecycleClient lifecycleClient,
      DeploymentInfo deploymentInfo,
      Callable<ComponentInstanceId> deployLogic) {

    ComponentInstanceId componentInstanceId = null;
    try {
      componentInstanceId = deployLogic.call();
    } catch (Exception e) {
      return failProcess(null, deploymentInfo, e);
    }

    try {
      waitForDeployment(lifecycleClient, componentInstanceId);
    } catch (Exception e) {
      return failProcess(componentInstanceId, deploymentInfo, e);
    }

    return convertToProcess(componentInstanceId, deploymentInfo);
  }

  private CloudiatorProcess deployPublicDockerComponent(DeploymentInfo deployInfo,
      LifecycleClient lifecycleClient) {
    LOGGER.debug(String
        .format("Creating Public Docker component for task %s.",
            deployInfo.task));

    final DockerComponent dockerComponent = new PublicDockerComponentSupplier(deployInfo.job,
        deployInfo.task).get();
    LOGGER.debug(
        String.format("Successfully build Public Docker component %s", dockerComponent));

    new RegisterTaskDeploymentContextVisitor(deployInfo.job, deployInfo.task)
        .visitDeploymentContext(deployInfo.deploymentContext);

    LOGGER.debug(String.format(
        "Calling client %s to deploy instance using: deploymentContext %s, Public Docker Component %s.",
        lifecycleClient, deployInfo.deploymentContext, dockerComponent));

    return callToDeploy(lifecycleClient, deployInfo, new Callable<ComponentInstanceId>() {
      @Override
      public ComponentInstanceId call() throws Exception {
        return lifecycleClient
            .deploy(deployInfo.deploymentContext, dockerComponent);
      }
    });
  }

  private void waitForDeployment(LifecycleClient client, ComponentInstanceId componentInstanceId) {
    client.waitForDeployment(componentInstanceId);
  }

  private CloudiatorProcess convertToProcess(ComponentInstanceId cId,
      DeploymentInfo deploymentInfo) {

    LOGGER.debug(String
        .format(
            "Client deployed the process of task %s with component instance id %s successfully",
            deploymentInfo.getTask(), cId));

    return CloudiatorSingleProcessBuilder.create().id(cId.toString()).originId(cId.toString())
        .state(ProcessState.RUNNING)
        .taskInterface(deploymentInfo.getTaskInterface().getClass().getCanonicalName())
        .userId(deploymentInfo.getUserId())
        .node(deploymentInfo.getNode().id())
        .type(Type.LANCE)
        .taskName(deploymentInfo.getTask().name()).scheduleId(deploymentInfo.getSchedule()).build();
  }

  private CloudiatorProcess failProcess(@Nullable ComponentInstanceId cId,
      DeploymentInfo deploymentInfo, Exception e) {

    String id;
    if (cId == null) {
      id = UUID.randomUUID().toString();
      LOGGER.warn("ComponentInstanceId was not generated, using a random one.");
    } else {
      id = cId.toString();
    }

    LOGGER.error(String
        .format(
            "Exception occurred while deployment the task %s. InstanceId is %s. Exception was %s.",
            deploymentInfo.getTask(), id, e.getMessage()), e);

    return CloudiatorSingleProcessBuilder.create().id(id).originId(id)
        .state(ProcessState.ERROR)
        .userId(deploymentInfo.getUserId())
        .node(deploymentInfo.getNode().id())
        .type(Type.LANCE)
        .taskInterface(deploymentInfo.getTaskInterface().getClass().getCanonicalName())
        .diagnostic(e.getMessage())
        .taskName(deploymentInfo.getTask().name()).scheduleId(deploymentInfo.getSchedule()).build();
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

  private static class DeploymentInfo {

    private final Task task;
    private final Job job;
    private final DeploymentContext deploymentContext;
    private final String schedule;
    private final Node node;
    private final String userId;
    private final TaskInterface taskInterface;

    private DeploymentInfo(Task task, Job job, DeploymentContext deploymentContext,
        String schedule, Node node, String userId, TaskInterface taskInterface) {
      this.task = task;
      this.job = job;
      this.deploymentContext = deploymentContext;
      this.schedule = schedule;
      this.node = node;
      this.userId = userId;
      this.taskInterface = taskInterface;
    }

    public Task getTask() {
      return task;
    }

    public Job getJob() {
      return job;
    }

    public DeploymentContext getDeploymentContext() {
      return deploymentContext;
    }

    public String getSchedule() {
      return schedule;
    }

    public Node getNode() {
      return node;
    }

    public String getUserId() {
      return userId;
    }

    public TaskInterface getTaskInterface() {
      return taskInterface;
    }
  }
}
