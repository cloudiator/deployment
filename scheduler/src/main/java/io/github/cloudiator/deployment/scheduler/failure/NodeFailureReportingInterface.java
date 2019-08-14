package io.github.cloudiator.deployment.scheduler.failure;

import io.github.cloudiator.domain.Node;

public interface NodeFailureReportingInterface {

  void addNodeFailure(Node node);

}
