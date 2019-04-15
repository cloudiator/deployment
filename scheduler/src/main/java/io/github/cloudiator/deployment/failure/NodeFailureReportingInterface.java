package io.github.cloudiator.deployment.failure;

import io.github.cloudiator.domain.Node;

public interface NodeFailureReportingInterface {

  void addNodeFailure(Node node);

}
