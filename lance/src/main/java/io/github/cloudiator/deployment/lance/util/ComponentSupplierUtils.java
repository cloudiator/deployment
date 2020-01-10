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
    portUpdateBuilder.setOperatingSystem(lanceInterface.operatingSystem());
    portUpdateBuilder.addCommand(lanceInterface.portUpdateAction().get());
    return portUpdateBuilder.buildPortUpdateHandler();
  }
}
