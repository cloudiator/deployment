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

import com.google.common.collect.ImmutableList;

public class ProcessGroupImpl implements ProcessGroup {

  private final Iterable<CloudiatorProcess> processes;

  public ProcessGroupImpl(
      Iterable<CloudiatorProcess> processes) {
    this.processes = ImmutableList.copyOf(processes);
  }

  @Override
  public Iterable<CloudiatorProcess> processes() {
    return processes;
  }
}
