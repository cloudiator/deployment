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
  private final Object lock = new Object();
  private volatile Map<String,ByonNode> byonNodeCache = new HashMap<>();
  private volatile boolean isAddedSync;

  @Override
  public synchronized void add(Node node, boolean isDirty) {
    //isDirty == true, sync needed -> done by node-agent, waiting for sync to be finished via lock
    //If unallocated via node-agent: add here and write through to byon-store via node-agent
    //If unallocated via byon-agent: add here and write into byon-store via byon-agent
    if(hit(node.id()).isPresent()) {
      LOGGER.warn(String.format("Overriding node with id %s in cache %s", node.id(), this));
    }

    if(isDirty) {
      dirtySyncAdd(node);
    }

    ByonNode byonNode = ByonNodeToNodeConverter.INSTANCE.applyBack(node);
    byonNodeCache.put(byonNode.id(), byonNode);
  }

  private void dirtySyncAdd(Node node) {

    synchronized (lock) {
      try {
        LOGGER.info(String.format("Waiting for add sync in cache: %s ", this));
        wait(timeoutMilli);
        LOGGER.info(String.format("Finished waiting for add sync in cache: %s ", this));
      } catch (InterruptedException e) {
        LOGGER.error(String.format("Got interrupted while syncing add operation for node with id"
            + "%s in cache %s.", node.id(), this));
      }
    }
  }

  @Override
  public synchronized Optional<Node> evict(String id, boolean isDirty) {
    //isDirty == true, sync needed -> done by node-agent, waiting for sync to be finished via lock
    if (!hit(id).isPresent()) {
      LOGGER.error(String.format("Cannot evict node with id %s in cache %s as id is not present", id, this));
      return Optional.empty();
    }

    ByonNode evictNode = byonNodeCache.get(id);

    if(isDirty) {
      dirtySyncEvict(id);
    }

    byonNodeCache.remove(id);
    return Optional.of(ByonNodeToNodeConverter.INSTANCE.apply(evictNode));
  }

  private void dirtySyncEvict(String id) {

    synchronized (lock) {
      try {
        LOGGER.info(String.format("Waiting for evict sync in cache: %s ", this));
        wait(timeoutMilli);
        LOGGER.info(String.format("Finished waiting for evict sync in cache: %s ", this));
      } catch (InterruptedException e) {
        LOGGER.error(String.format("Got interrupted while syncing eviction operation for node with id"
            + "%s in cache %s.", id, this));
      }
    }
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

  @Override
  public synchronized void synchronize() {
    synchronized (lock) {
      notify();
    }
  }


}
