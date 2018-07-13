/*
 * Copyright 2018 University of Ulm
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.domain.Node;

public class LanceProcessImpl implements LanceProcess {

  private final String id;
  private final Schedule schedule;
  private final String task;
  private final Node node;


  public LanceProcessImpl(String id, Schedule schedule, String task,
      Node node) {
    checkNotNull(schedule, "schedule is null");
    this.schedule = schedule;
    checkNotNull(task, "task is null");
    checkArgument(!task.isEmpty(), "task is empty");
    this.task = task;
    checkNotNull(node, "node is null");
    this.node = node;
    checkNotNull(id, "id is null");
    checkArgument(!id.isEmpty(), "id is empty");
    this.id = id;
  }

  @Override
  public Schedule schedule() {
    return schedule;
  }

  @Override
  public String task() {
    return task;
  }

  @Override
  public Node node() {
    return node;
  }

  @Override
  public String id() {
    return id;
  }
}
