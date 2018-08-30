/*
 * Copyright 2017 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

class CloudiatorProcessImpl implements CloudiatorProcess {

  private final String id;
  private final String jobId;
  private final String taskName;
  private final String nodeId;
  private final CloudiatorProcess.State state;

  CloudiatorProcessImpl(String id, String jobId, String taskName, String nodeId,
      State state) {
    this.id = id;
    this.jobId = jobId;
    this.taskName = taskName;
    this.nodeId = nodeId;
    this.state = state;
  }

  @Override
  public String jobId() {
    return jobId;
  }

  @Override
  public String taskId() {
    return taskName;
  }

  @Override
  public State state() {
    return state;
  }

  @Override
  public String nodeId() {
    return nodeId;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudiatorProcessImpl that = (CloudiatorProcessImpl) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(jobId, that.jobId) &&
        Objects.equals(taskName, that.taskName) &&
        Objects.equals(nodeId, that.nodeId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, jobId, taskName, nodeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("job", jobId).add("task", taskName)
        .add("node", nodeId).add("state", state).toString();
  }
}
