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

public class ProcessBuilder {

  private String id;
  private Task task;
  private Process.State state;

  private ProcessBuilder() {
  }

  public static ProcessBuilder newBuilder() {
    return new ProcessBuilder();
  }

  public ProcessBuilder id(String id) {
    this.id = id;
    return this;
  }

  public ProcessBuilder task(Task task) {
    this.task = task;
    return this;
  }

  public ProcessBuilder state(Process.State state) {
    this.state = state;
    return this;
  }

  public Process build() {
    return new ProcessImpl(id, task, state);
  }

}
