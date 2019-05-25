package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import java.util.stream.Collectors;

public class ByonNodePool implements ExistingNodePool {

  private final ByonNodeCache cache;

  @Inject
  public ByonNodePool(ByonNodeCache cache) {
    this.cache = cache;
  }

  @Override
  public Set<Node> getAll() {
    return cache.readAll().stream()
        .collect(Collectors.toSet());
  }
}
