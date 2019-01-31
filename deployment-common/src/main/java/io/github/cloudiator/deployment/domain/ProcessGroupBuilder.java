/*
 * Copyright 2014-2019 University of Ulm
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProcessGroupBuilder {

  private ProcessGroupBuilder() {
    processSet = new HashSet<>();
  }

  private String id;
  private String userId;
  private String scheduleId;
  private Set<CloudiatorProcess> processSet;

  public static ProcessGroupBuilder create() {
    return new ProcessGroupBuilder();
  }

  public ProcessGroupBuilder id(String id) {
    this.id = id;
    return this;
  }

  public ProcessGroupBuilder generateId() {
    this.id = UUID.randomUUID().toString();
    return this;
  }

  public ProcessGroupBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  public ProcessGroupBuilder scheduleId(String scheduleId) {
    this.scheduleId = scheduleId;
    return this;
  }

  public ProcessGroupBuilder addProcess(CloudiatorProcess cloudiatorProcess) {
    processSet.add(cloudiatorProcess);
    return this;
  }

  public ProcessGroupBuilder addProcesses(Collection<? extends CloudiatorProcess> processes) {
    processSet.addAll(processes);
    return this;
  }

  public ProcessGroup build() {
    return new ProcessGroupImpl(id, userId, scheduleId, processSet);
  }


}
