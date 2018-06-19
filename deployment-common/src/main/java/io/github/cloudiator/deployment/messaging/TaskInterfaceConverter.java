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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.TaskInterface;
import org.cloudiator.messages.entities.TaskEntities;

public class TaskInterfaceConverter implements
    TwoWayConverter<TaskEntities.TaskInterface, TaskInterface> {

  public static final TaskInterfaceConverter INSTANCE = new TaskInterfaceConverter();

  private static final LanceInterfaceConverter LANCE_INTERFACE_CONVERTER = LanceInterfaceConverter.INSTANCE;

  private TaskInterfaceConverter() {

  }

  @Override
  public TaskEntities.TaskInterface applyBack(TaskInterface taskInterface) {

    if (taskInterface instanceof LanceInterface) {
      return TaskEntities.TaskInterface.newBuilder()
          .setLanceInterface(LANCE_INTERFACE_CONVERTER.applyBack(
              (LanceInterface) taskInterface)).build();
    } else {
      throw new AssertionError(
          "Unknown type for task interface " + taskInterface.getClass().getName());
    }
  }

  @Override
  public TaskInterface apply(TaskEntities.TaskInterface taskInterface) {

    switch (taskInterface.getTaskInterfaceCase()) {
      case DOCKERINTERFACE:
        throw new UnsupportedOperationException("DockerInterface is not yet implemented");
      case LANCEINTERFACE:
        return LANCE_INTERFACE_CONVERTER.apply(taskInterface.getLanceInterface());
      case TASKINTERFACE_NOT_SET:
        throw new AssertionError("TaskInterface is not set");
      default:
        throw new AssertionError(
            "Unknown TaskInterfaceCase " + taskInterface.getTaskInterfaceCase());
    }

  }
}
