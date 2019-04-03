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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CloudiatorClusterProcessBuilder {

  private String id;
  private String originId;
  private String userId;
  private String scheduleId;
  private String taskName;
  private CloudiatorProcess.ProcessState state;
  private Set<String> nodes;
  private Type type;
  private String reason;
  private String diagnostic;

  private CloudiatorClusterProcessBuilder() {
    this.nodes = new HashSet<>();
  }

  private CloudiatorClusterProcessBuilder(CloudiatorClusterProcess cloudiatorClusterProcess) {
    this.id = cloudiatorClusterProcess.id();
    this.originId = cloudiatorClusterProcess.originId().orElse(null);
    this.userId = cloudiatorClusterProcess.userId();
    this.scheduleId = cloudiatorClusterProcess.scheduleId();
    this.taskName = cloudiatorClusterProcess.taskId();
    this.state = cloudiatorClusterProcess.state();
    this.nodes = cloudiatorClusterProcess.nodes();
    this.type = cloudiatorClusterProcess.type();
    this.reason = cloudiatorClusterProcess.reason().orElse(null);
    this.diagnostic = cloudiatorClusterProcess.diagnostic().orElse(null);
  }

  public static CloudiatorClusterProcessBuilder create() {
    return new CloudiatorClusterProcessBuilder();
  }

  public static CloudiatorClusterProcessBuilder of(
      CloudiatorClusterProcess cloudiatorClusterProcess) {
    checkNotNull(cloudiatorClusterProcess, "cloudiatorClusterProcess is null");
    return new CloudiatorClusterProcessBuilder(cloudiatorClusterProcess);
  }

  public CloudiatorClusterProcessBuilder id(String id) {
    this.id = id;
    return this;
  }

  public CloudiatorClusterProcessBuilder originId(String originId) {
    this.originId = originId;
    return this;
  }

  public CloudiatorClusterProcessBuilder userId(String userId) {
    this.userId = userId;
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

  public CloudiatorClusterProcessBuilder addNode(String node) {
    this.nodes.add(node);
    return this;
  }

  public CloudiatorClusterProcessBuilder addAllNodes(Collection<? extends String> nodes) {
    this.nodes.addAll(nodes);
    return this;
  }

  public CloudiatorClusterProcessBuilder state(CloudiatorProcess.ProcessState state) {
    this.state = state;
    return this;
  }

  public CloudiatorClusterProcessBuilder type(Type type) {
    this.type = type;
    return this;
  }

  public CloudiatorClusterProcessBuilder reason(String reason) {
    this.reason = reason;
    return this;
  }

  public CloudiatorClusterProcessBuilder diagnostic(String diagnostic) {
    this.diagnostic = diagnostic;
    return this;
  }

  public CloudiatorClusterProcess build() {
    return new CloudiatorClusterProcessImpl(id, originId, userId, scheduleId, taskName, state, type,
        nodes, diagnostic, reason);
  }

}
