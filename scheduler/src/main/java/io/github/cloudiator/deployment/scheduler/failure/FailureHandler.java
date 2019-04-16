package io.github.cloudiator.deployment.scheduler.failure;

import com.google.inject.Singleton;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.domain.Node;

@Singleton
public class FailureHandler implements NodeFailureReportingInterface,
    ProcessFailureReportingInterface {


  public void addNodeFailure(Node node) {

  }

  public void addProcessFailure(CloudiatorProcess cloudiatorProcess) {

  }

}
