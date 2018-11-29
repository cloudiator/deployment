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

import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;

public class CloudiatorClusterProcessBuilder {

  private String id;
  private String scheduleId;
  private String taskName;
  private CloudiatorProcess.State state;
  private String nodeGroup;
  private Type type;

  private CloudiatorClusterProcessBuilder() {
  }

  public static CloudiatorClusterProcessBuilder newBuilder() {
    return new CloudiatorClusterProcessBuilder();
  }

  public CloudiatorClusterProcessBuilder id(String id) {
    this.id = id;
    return this;
  }

  public CloudiatorClusterProcessBuilder taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public CloudiatorClusterProcessBuilder scheduleId(String scheduleId) {
    this.scheduleId = scheduleId;
    return this;
  }

  public CloudiatorClusterProcessBuilder nodeGroup(String nodeGroup) {
    this.nodeGroup = nodeGroup;
    return this;
  }

  public CloudiatorClusterProcessBuilder state(CloudiatorProcess.State state) {
    this.state = state;
    return this;
  }

  public CloudiatorClusterProcessBuilder type(Type type) {
    this.type = type;
    return this;
  }

  public CloudiatorClusterProcess build() {
    return new CloudiatorClusterProcessImpl(id, scheduleId, taskName,  state, type, nodeGroup);
  }

}
