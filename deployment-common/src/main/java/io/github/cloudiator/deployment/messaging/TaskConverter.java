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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.Task.Builder;

public class TaskConverter implements TwoWayConverter<TaskEntities.Task, Task> {

  //todo add interfaces

  private static final PortConverter PORT_CONVERTER = new PortConverter();

  @Override
  public TaskEntities.Task applyBack(Task task) {

    Builder builder = TaskEntities.Task.newBuilder();
    builder.setName(task.name());

    task.ports().stream().map(PORT_CONVERTER::applyBack).forEach(builder::addPorts);

    return builder.build();
  }

  @Override
  public Task apply(TaskEntities.Task task) {

    final TaskBuilder taskBuilder = TaskBuilder.newBuilder().name(task.getName());

    task.getPortsList().forEach(port -> taskBuilder.addPort(PORT_CONVERTER.apply(port)));

    return taskBuilder.build();
  }
}
