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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class TaskModelConverter implements OneWayConverter<TaskModel, Task> {

  private final TaskInterfaceModelConverter taskInterfaceConverter = new TaskInterfaceModelConverter();
  private final PortModelConverter portConverter = new PortModelConverter();

  @Nullable
  @Override
  public Task apply(@Nullable TaskModel taskModel) {
    if (taskModel == null) {
      return null;
    }

    return TaskBuilder.newBuilder().name(taskModel.getName())
        .addPorts(taskModel.getPorts().stream().map(portConverter).collect(Collectors.toList()))
        .addInterfaces(taskModel.getInterfaces().stream().map(taskInterfaceConverter).collect(
            Collectors.toList())).build();
  }
}
