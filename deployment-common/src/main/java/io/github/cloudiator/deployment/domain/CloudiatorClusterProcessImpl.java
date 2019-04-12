package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorClusterProcessImpl extends CloudiatorProcessImpl implements
    CloudiatorClusterProcess {

  private final Set<String> nodes;


  CloudiatorClusterProcessImpl(String id, @Nullable String originId, String userId,
      String scheduleId, String taskName, String lifecycleInterface,
      CloudiatorProcess.ProcessState state, Type type, Set<String> nodes,
      @Nullable String diagnostic, @Nullable String reason) {
    super(id, originId, userId, scheduleId, taskName, lifecycleInterface, state, type, diagnostic,
        reason);

    checkNotNull(nodes, "nodes is null");
    this.nodes = new HashSet<>(nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, scheduleId, taskName, nodes);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudiatorClusterProcessImpl that = (CloudiatorClusterProcessImpl) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(userId, that.userId) &&
        Objects.equals(scheduleId, that.scheduleId) &&
        Objects.equals(taskName, that.taskName) &&
        Objects.equals(nodes, that.nodes);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("nodes", nodes);
  }

  @Override
  public Set<String> nodes() {
    return ImmutableSet.copyOf(nodes);
  }
}
