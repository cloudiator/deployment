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

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.Optimization;
import org.cloudiator.matchmaking.domain.Requirement;

class TaskImpl implements Task {

  private final String name;
  private final Set<Port> ports;
  private final Set<TaskInterface> interfaces;
  private final Set<Requirement> requirements;
  @Nullable
  private final Optimization optimization;

  TaskImpl(String name, Set<Port> ports,
      Set<TaskInterface> interfaces,
      Set<Requirement> requirements, @Nullable Optimization optimization) {

    checkNotNull(name, "name is null");
    checkArgument(!name.isEmpty(), "name is empty");
    checkNotNull(ports, "ports is null");
    checkNotNull(interfaces, "interfaces is null");
    checkNotNull(requirements, "requirements is null");

    this.name = name;
    this.ports = ImmutableSet.copyOf(ports);
    this.interfaces = ImmutableSet.copyOf(interfaces);
    this.requirements = ImmutableSet.copyOf(requirements);
    this.optimization = optimization;
  }


  @Override
  public String name() {
    return name;
  }

  @Override
  public Set<Port> ports() {
    return ports;
  }

  @Override
  public Set<TaskInterface> interfaces() {
    return interfaces;
  }

  @Override
  public Set<Requirement> requirements() {
    return requirements;
  }

  @Override
  public Optional<Optimization> optimization() {
    return Optional.ofNullable(optimization);
  }
}
