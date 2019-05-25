package io.github.cloudiator.deployment.scheduler.instantiation;

import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Optional;

public interface ExistingNodeCache {

  //cache unallocated Node
  Optional<Node> add(Node node);

  //remove now allocated or removed Node
  Optional<Node> evict(String id);

  Optional<Node> hit(String id);

  Optional<Node> read(String id);

  List<Node> readAll();
}
