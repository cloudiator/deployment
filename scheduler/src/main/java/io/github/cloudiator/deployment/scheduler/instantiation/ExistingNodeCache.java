package io.github.cloudiator.deployment.scheduler.instantiation;

import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Optional;

public interface ExistingNodeCache {

  //cache unallocated Node
  //isDirty==true -> needs sync
  void add(Node node, boolean isDirty);

  //remove now allocated or removed Node
  //isDirty==true -> needs sync
  Optional<Node> evict(String id, boolean isDirty);

  Optional<Node> hit(String id);

  Optional<Node> read(String id);

  List<Node> readAll();

  void synchronize();
}
