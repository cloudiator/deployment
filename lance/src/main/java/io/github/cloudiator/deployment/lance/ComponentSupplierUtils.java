package io.github.cloudiator.deployment.lance;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemImpl;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import de.uniulm.omi.cloudiator.lance.application.component.OutPort;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties;
import de.uniulm.omi.cloudiator.lance.application.component.PortProperties.PortType;
import de.uniulm.omi.cloudiator.lance.client.DeploymentHelper;
import de.uniulm.omi.cloudiator.lance.lifecycle.bash.BashBasedHandlerBuilder;
import de.uniulm.omi.cloudiator.lance.lifecycle.detector.PortUpdateHandler;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;

public class ComponentSupplierUtils {

  private static final OperatingSystem staticOs;

  static {
    staticOs = new OperatingSystemImpl(
        OperatingSystemFamily.UBUNTU,
        OperatingSystemArchitecture.AMD64,
        OperatingSystemVersions.of(1404,"14.04"));
  }

  private ComponentSupplierUtils() {
  }

  public static PortType derivePortType(Job job, PortProvided provided) {
    if (job.attachedCommunications(provided).isEmpty()) {
      return PortProperties.PortType.PUBLIC_PORT;
    } else {
      // todo should be internal, but for the time being we use public here
      // todo facilitates the security group handling
      // todo portType = PortProperties.PortType.INTERNAL_PORT;
      return PortProperties.PortType.PUBLIC_PORT;
    }
  }

  public static int deriveMinSinks(PortRequired portRequired) {
    if (portRequired.isMandatory()) {
      return 1;
    }
    return OutPort.NO_SINKS;
  }

  public static PortUpdateHandler portUpdateHandler(LanceInterface lanceInterface) {
    if (!lanceInterface.portUpdateAction().isPresent()) {
      return DeploymentHelper.getEmptyPortUpdateHandler();
    }

    //todo this is inconsistent. multiple PortUpdateHandler should be allowed here so
    //todo it is possible to set one per operating system!
    BashBasedHandlerBuilder portUpdateBuilder = new BashBasedHandlerBuilder();
    portUpdateBuilder.setOperatingSystem(staticOs);
    portUpdateBuilder.addCommand(lanceInterface.portUpdateAction().get());
    return portUpdateBuilder.buildPortUpdateHandler();
  }
}
