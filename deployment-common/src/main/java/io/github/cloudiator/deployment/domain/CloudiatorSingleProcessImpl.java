package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorSingleProcessImpl extends CloudiatorProcessImpl implements
    CloudiatorSingleProcess {


  private final String node;

  CloudiatorSingleProcessImpl(String id, @Nullable String originId, String userId,
      String scheduleId,
      String taskName,
      String taskInterface,
      ProcessState state, Type type, String node, @Nullable String diagnostic,
      @Nullable String reason, @Nullable String endpoint, Set<IpAddress> ipAddresses) {
    super(id, originId, userId, scheduleId, taskName, taskInterface, state, type, diagnostic,
        reason, endpoint, ipAddresses);

    checkNotNull(node, "node is null");
    checkArgument(!node.isEmpty(), "node is empty");
    this.node = node;

  }

  @Override
  public String node() {
    return node;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudiatorSingleProcess that = (CloudiatorSingleProcess) o;
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
        node().equals(that.node());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id(), originId(), userId(), scheduleId(), taskId(), taskInterface(), state(), type(),
            diagnostic(),
            reason(), endpoint(), ipAddresses(), node());
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("node", node);
  }

  @Override
  public Set<String> nodes() {
    return Collections.singleton(node);
  }
}
