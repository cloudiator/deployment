package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorSingleProcessImpl extends CloudiatorProcessImpl implements CloudiatorSingleProcess {


  private final String node;

  CloudiatorSingleProcessImpl(String id, String scheduleId, String taskName,
      State state, Type type, String node) {
    super(id, scheduleId, taskName, state, type);


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

    return Objects.hash(id, scheduleId, taskName, node);
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
        Objects.equals(scheduleId, that.scheduleId) &&
        Objects.equals(taskName, that.taskName) &&
        Objects.equals(node, that.node);
  }



  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("scheduleId", scheduleId)
        .add("task", taskName)
        .add("node", node).add("state", state).toString();
  }


}
