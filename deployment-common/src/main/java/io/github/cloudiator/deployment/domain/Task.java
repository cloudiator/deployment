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

import de.uniulm.omi.cloudiator.util.StreamUtil;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.domain.Optimization;
import org.cloudiator.matchmaking.domain.Requirement;

/**
 * Created by daniel on 13.02.17.
 */
public interface Task {

  String name();

  Set<Port> ports();

  Set<TaskInterface> interfaces();
  
  default <T extends TaskInterface> T interfaceOfType(Class<T> type) {
    //noinspection unchecked
    return interfaces().stream().filter(
        type::isInstance).map(taskInterface -> (T) taskInterface)
        .collect(StreamUtil.getOnly())
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Task %s does not supply a interface of type %s.", this, type)));
  }

  Set<Requirement> requirements();

  Set<Requirement> requirements(Job fallback);

  Optional<Optimization> optimization();

  default Set<PortProvided> providedPorts() {
    return ports(PortProvided.class);
  }

  default Set<PortRequired> requiredPorts() {
    return ports(PortRequired.class);
  }

  default <T extends Port> Set<T> ports(Class<T> type) {

    checkNotNull(type, "type is null");

    //noinspection unchecked
    return this.ports().stream().filter(type::isInstance)
        .map(port -> (T) port).collect(Collectors.toSet());
  }

  default <T extends Port> Optional<T> findPort(String name, Class<T> type) {
    checkNotNull(name, "name is null");
    checkNotNull(type, "type is null");

    return ports(type).stream().filter(t -> name.equals(t.name())).collect(StreamUtil.getOnly());
  }

  Behaviour behaviour();

}
