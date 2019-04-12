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

public class CloudiatorSingleProcessBuilder {

  private String id;
  private String originId;
  private String userId;
  private String scheduleId;
  private String taskName;
  private String taskInterface;
  private CloudiatorProcess.ProcessState state;
  private String node;
  private Type type;
  private String reason;
  private String diagnostic;

  private CloudiatorSingleProcessBuilder() {
  }

  private CloudiatorSingleProcessBuilder(CloudiatorSingleProcess cloudiatorSingleProcess) {
    this.id = cloudiatorSingleProcess.id();
    this.originId = cloudiatorSingleProcess.originId().orElse(null);
    this.userId = cloudiatorSingleProcess.userId();
    this.scheduleId = cloudiatorSingleProcess.scheduleId();
    this.taskName = cloudiatorSingleProcess.taskId();
    this.taskInterface = cloudiatorSingleProcess.taskInterface();
    this.state = cloudiatorSingleProcess.state();
    this.node = cloudiatorSingleProcess.node();
    this.type = cloudiatorSingleProcess.type();
    this.reason = cloudiatorSingleProcess.reason().orElse(null);
    this.diagnostic = cloudiatorSingleProcess.diagnostic().orElse(null);
  }

  public static CloudiatorSingleProcessBuilder create() {
    return new CloudiatorSingleProcessBuilder();
  }

  public static CloudiatorSingleProcessBuilder of(CloudiatorSingleProcess cloudiatorSingleProcess) {
    checkNotNull(cloudiatorSingleProcess, "cloudiatorSingleProcess is null");
    return new CloudiatorSingleProcessBuilder(cloudiatorSingleProcess);
  }

  public CloudiatorSingleProcessBuilder id(String id) {
    this.id = id;
    return this;
  }

  public CloudiatorSingleProcessBuilder originId(String originId) {
    this.originId = originId;
    return this;
  }

  public CloudiatorSingleProcessBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  public CloudiatorSingleProcessBuilder taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public CloudiatorSingleProcessBuilder taskInterface(String taskInterface) {
    this.taskInterface = taskInterface;
    return this;
  }

  public CloudiatorSingleProcessBuilder scheduleId(String scheduleId) {
    this.scheduleId = scheduleId;
    return this;
  }

  public CloudiatorSingleProcessBuilder node(String node) {
    this.node = node;
    return this;
  }

  public CloudiatorSingleProcessBuilder state(CloudiatorProcess.ProcessState state) {
    this.state = state;
    return this;
  }

  public CloudiatorSingleProcessBuilder type(Type type) {
    this.type = type;
    return this;
  }

  public CloudiatorSingleProcessBuilder reason(String reason) {
    this.reason = reason;
    return this;
  }

  public CloudiatorSingleProcessBuilder diagnostic(String diagnostic) {
    this.diagnostic = diagnostic;
    return this;
  }

  public CloudiatorSingleProcess build() {
    return new CloudiatorSingleProcessImpl(id, originId, userId, scheduleId, taskName,
        taskInterface, state, type,
        node,
        diagnostic, reason);
  }

}
