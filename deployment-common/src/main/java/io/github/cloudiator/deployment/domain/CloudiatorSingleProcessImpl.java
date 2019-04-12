package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
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
      @Nullable String reason) {
    super(id, originId, userId, scheduleId, taskName, taskInterface, state, type, diagnostic,
        reason);

    checkNotNull(node, "node is null");
    checkArgument(!node.isEmpty(), "node is empty");
    this.node = node;

  }

  @Override
  public String node() {
    return node;
  }


  @Override
  public int hashCode() {
    return Objects.hash(id, userId, scheduleId, taskName, node);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudiatorSingleProcessImpl that = (CloudiatorSingleProcessImpl) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(userId, that.userId) &&
        Objects.equals(scheduleId, that.scheduleId) &&
        Objects.equals(taskName, that.taskName) &&
        Objects.equals(node, that.node);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("node", node);
  }
}
