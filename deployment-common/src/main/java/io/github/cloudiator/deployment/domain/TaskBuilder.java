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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.domain.Optimization;
import org.cloudiator.matchmaking.domain.Requirement;

public class TaskBuilder {

  private String name;
  private Set<Port> ports;
  private Set<TaskInterface> interfaces;
  private Set<Requirement> requirements;
  private Optimization optimization;
  private Behaviour behaviour;

  private TaskBuilder() {
    ports = new HashSet<>();
    interfaces = new HashSet<>();
    requirements = new HashSet<>();
  }

  public static TaskBuilder newBuilder() {
    return new TaskBuilder();
  }

  public TaskBuilder name(String name) {
    this.name = name;
    return this;
  }

  public TaskBuilder addPort(Port port) {
    this.ports.add(port);
    return this;
  }

  public TaskBuilder addPorts(Collection<? extends Port> ports) {
    this.ports.addAll(ports);
    return this;
  }

  public TaskBuilder addInterface(TaskInterface taskInterface) {
    this.interfaces.add(taskInterface);
    return this;
  }

  public TaskBuilder addInterfaces(Collection<? extends TaskInterface> taskInterfaces) {
    this.interfaces.addAll(taskInterfaces);
    return this;
  }

  public TaskBuilder addRequirement(Requirement requirement) {
    this.requirements.add(requirement);
    return this;
  }

  public TaskBuilder addRequirements(Collection<? extends Requirement> requirements) {
    this.requirements.addAll(requirements);
    return this;
  }

  public TaskBuilder optimization(Optimization optimization) {
    this.optimization = optimization;
    return this;
  }

  public TaskBuilder behaviour(Behaviour behaviour) {
    this.behaviour = behaviour;
    return this;
  }

  public Task build() {
    return new TaskImpl(name, ports, interfaces, requirements, optimization, behaviour);
  }

}
