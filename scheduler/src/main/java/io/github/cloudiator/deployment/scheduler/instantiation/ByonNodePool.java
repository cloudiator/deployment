package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import java.util.List;

public class ByonNodePool implements ExistingNodePool {

  private final ByonNodeCache cache;

  @Inject
  public ByonNodePool(ByonNodeCache cache) {
    this.cache = cache;
  }

  @Override
  public List<Node> getAll() {
    return cache.readAll();
  }
}
