package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
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
      @Nullable String diagnostic, @Nullable String reason, @Nullable String endpoint,
      Set<IpAddress> ipAddresses) {
    super(id, originId, userId, scheduleId, taskName, lifecycleInterface, state, type, diagnostic,
        reason, endpoint, ipAddresses);

    checkNotNull(nodes, "nodes is null");
    this.nodes = new HashSet<>(nodes);
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
    return id().equals(that.id()) &&
        originId().equals(that.originId()) &&
        userId().equals(that.userId()) &&
        scheduleId().equals(that.scheduleId()) &&
        taskId().equals(that.taskId()) &&
        taskInterface().equals(that.taskInterface()) &&
        state() == that.state() &&
        type() == that.type() &&
        Objects.equals(diagnostic(), that.diagnostic()) &&
        Objects.equals(reason(), that.reason()) &&
        Objects.equals(endpoint(), that.endpoint()) &&
        ipAddresses().equals(that.ipAddresses()) &&
        nodes().equals(that.nodes());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id(), originId(), userId(), scheduleId(), taskId(), taskInterface(), state(), type(),
            diagnostic(),
            reason(), endpoint(), ipAddresses(), nodes());
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
