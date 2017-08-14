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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class ProcessImpl implements Process {

  private final String id;
  private final Task task;
  private final Process.State state;

  ProcessImpl(String id, Task task,
      State state) {

    checkNotNull(id, "id is null");
    checkArgument(!id.isEmpty(), "id is empty");
    checkNotNull(task, "task is null");
    checkNotNull(state, "state is null");

    this.id = id;
    this.task = task;
    this.state = state;
  }


  @Override
  public Task task() {
    return task;
  }

  @Override
  public State state() {
    return state;
  }

  @Override
  public String id() {
    return id;
  }
}
