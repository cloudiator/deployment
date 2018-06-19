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

public class InterfaceConverter implements
    TwoWayConverter<TaskEntities.TaskInterface, TaskInterface> {

  public final static InterfaceConverter INSTANCE = new InterfaceConverter();

  private InterfaceConverter() {

  }


  @Override
  public TaskEntities.TaskInterface applyBack(TaskInterface taskInterface) {
    if (taskInterface instanceof LanceInterface) {
      return TaskEntities.TaskInterface.newBuilder().setLanceInterface(
          LanceInterfaceConverter.INSTANCE.applyBack((LanceInterface) taskInterface)).build();
    } else {
      throw new AssertionError(String
          .format("TaskInterface is of illegal type %s.", taskInterface.getClass().getName()));
    }
  }

  @Override
  public TaskInterface apply(TaskEntities.TaskInterface taskInterface) {
    switch (taskInterface.getTaskInterfaceCase()) {
      case LANCEINTERFACE:
        return LanceInterfaceConverter.INSTANCE.apply(taskInterface.getLanceInterface());
      case DOCKERINTERFACE:
        throw new UnsupportedOperationException("DockerInterface not implemented yet");
      case TASKINTERFACE_NOT_SET:
        throw new AssertionError("TaskInterface was not set.");
      default:
        throw new AssertionError(
            "TaskInterface is of illegal case " + taskInterface.getTaskInterfaceCase());
    }
  }
}
