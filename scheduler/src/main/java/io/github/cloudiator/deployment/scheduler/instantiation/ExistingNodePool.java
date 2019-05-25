package io.github.cloudiator.deployment.scheduler.instantiation;

import io.github.cloudiator.domain.Node;
import java.util.List;

public interface ExistingNodePool {

  List<Node> getAll();
}
