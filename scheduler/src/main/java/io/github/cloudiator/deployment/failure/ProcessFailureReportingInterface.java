package io.github.cloudiator.deployment.failure;

import io.github.cloudiator.deployment.domain.CloudiatorProcess;

public interface ProcessFailureReportingInterface {

  void addProcessFailure(CloudiatorProcess cloudiatorProcess);

}
