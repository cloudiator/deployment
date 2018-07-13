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

import io.github.cloudiator.domain.Node;

public class LanceProcessBuilder {

  private String id;
  private Schedule schedule;
  private String task;
  private Node node;

  private LanceProcessBuilder() {
  }

  public static LanceProcessBuilder newBuilder() {
    return new LanceProcessBuilder();
  }

  public LanceProcessBuilder id(String id) {
    this.id = id;
    return this;
  }

  public LanceProcessBuilder schedule(Schedule schedule) {
    this.schedule = schedule;
    return this;
  }

  public LanceProcessBuilder task(String task) {
    this.task = task;
    return this;
  }

  public LanceProcessBuilder node(Node node) {
    this.node = node;
    return this;
  }

  public LanceProcess build() {
    return new LanceProcessImpl(id, schedule, task, node);
  }


}
