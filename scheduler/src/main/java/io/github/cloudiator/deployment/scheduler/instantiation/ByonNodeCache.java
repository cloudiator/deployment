package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.inject.Singleton;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeToNodeConverter;
import io.github.cloudiator.domain.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ByonNodeCache implements ExistingNodeCache {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonNodeCache.class);
  private static int timeoutMilli = 3000;
  private volatile Map<String,ByonNode> byonNodeCache = new HashMap<>();
  private volatile boolean isAddedSync;

  @Override
  public synchronized Optional<Node> add(Node node) {
    if(hit(node.id()).isPresent()) {
      LOGGER.info(String.format("Overriding node with id %s in cache %s.",
          node.id(), this));
    }

    ByonNode byonNode = ByonNodeToNodeConverter.INSTANCE.applyBack(node);
    ByonNode origByonNode = byonNodeCache.put(byonNode.id(), byonNode);
    Node returnNode = ByonNodeToNodeConverter.INSTANCE.apply(origByonNode);

    return Optional.ofNullable(returnNode);
  }

  @Override
  public synchronized Optional<Node> evict(String id) {
    if (!hit(id).isPresent()) {
      LOGGER.error(String.format("Cannot evict node with id %s in cache %s as id is not present", id, this));
      return Optional.empty();
    }

    ByonNode evictNode = byonNodeCache.get(id);

    byonNodeCache.remove(id);
    return Optional.of(ByonNodeToNodeConverter.INSTANCE.apply(evictNode));
  }

  @Override
  public synchronized Optional<Node> hit(String id) {
    return (byonNodeCache.get(id) == null) ? Optional.empty()
        : Optional.of(ByonNodeToNodeConverter.INSTANCE.apply(byonNodeCache.get(id)));
  }

  @Override
  public synchronized  Optional<Node> read(String id) {
    if (!hit(id).isPresent()) {
      LOGGER.error(String.format("Cannot read node with id %s in cache %s as id is not present", id, this));
      return Optional.empty();
    }

    return Optional.of(ByonNodeToNodeConverter.INSTANCE.apply(byonNodeCache.get(id)));
  }

  @Override
  public List<Node> readAll() {
    return byonNodeCache.values().stream().map(ByonNodeToNodeConverter.INSTANCE::apply).collect(
        Collectors.toList());
  }
}
