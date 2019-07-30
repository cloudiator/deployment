package io.github.cloudiator.deployment.lance;

import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.application.component.DockerComponent;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties;
import de.uniulm.omi.cloudiator.lance.client.DeploymentHelper;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import java.util.function.Supplier;

//dockerhub
public class PublicDockerComponentSupplier extends DockerComponentSupplier implements
    Supplier<DockerComponent> {

  PublicDockerComponentSupplier(Job job, Task task, DockerInterface dockerInterface) {
    super(job, task, dockerInterface);
  }

  @Override
  public DockerComponent get() {
    DockerComponent.Builder builder = new DockerComponent.Builder(deriveEntireCommands(),
        getActualImageName());
    builder.name(task.name());
    builder.myId(ComponentId.fromString(job.id() + "/" + task.name()));
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

    builder.deploySequentially(true);
    return builder.build();
  }
}
