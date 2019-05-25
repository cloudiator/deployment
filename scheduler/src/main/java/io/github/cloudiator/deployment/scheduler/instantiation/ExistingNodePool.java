package io.github.cloudiator.deployment.scheduler.instantiation;

import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Set;

public interface ExistingNodePool {

  Set<Node> getAll();
}
