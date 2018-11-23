package io.github.cloudiator.deployment.lance;

import de.uniulm.omi.cloudiator.lance.application.component.ComponentId;
import de.uniulm.omi.cloudiator.lance.application.component.DockerComponent;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties;
import de.uniulm.omi.cloudiator.lance.application.component.RemoteDockerComponent;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import java.util.function.Supplier;

public class PrivateDockerComponentSupplier extends DockerComponentSupplier implements
    Supplier<RemoteDockerComponent> {

  PrivateDockerComponentSupplier(Job job, Task task) {
    super(job, task);
  }

  @Override
  public RemoteDockerComponent get() {
    DockerComponent.Builder builder = new DockerComponent.Builder(deriveEntireCommands(), getActualImageName());
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
      builder.addOutport(required.name(), ComponentSupplierUtils.portUpdateHandler(required),
          PortProperties.INFINITE_CARDINALITY, ComponentSupplierUtils.deriveMinSinks(required));
    }

    RemoteDockerComponent.DockerRegistry dReg = new RemoteDockerComponent.DockerRegistry(getHostName(), getPort(), getCredentials().username, getCredentials().password, true);
    RemoteDockerComponent rDockerComp = new RemoteDockerComponent(builder, dReg);

    return rDockerComp;
  }
}
