package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorClusterProcessImpl extends CloudiatorProcessImpl implements
    CloudiatorClusterProcess {

  private final String nodeGroup;


  CloudiatorClusterProcessImpl(String id, String userId, String scheduleId, String taskName,
      CloudiatorProcess.ProcessState state, Type type, String nodeGroup,
      @Nullable String diagnostic, @Nullable String reason) {
    super(id, userId, scheduleId, taskName, state, type, diagnostic, reason);

    checkNotNull(nodeGroup, "nodeGroup is null");
    checkArgument(!nodeGroup.isEmpty(), "nodeGroup is empty");
    this.nodeGroup = nodeGroup;
  }


  @Override
  public String nodeGroup() {
    return nodeGroup;
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, userId, scheduleId, taskName, nodeGroup);
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
        Objects.equals(nodeGroup, that.nodeGroup);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("nodeGroup", nodeGroup);
  }
}
