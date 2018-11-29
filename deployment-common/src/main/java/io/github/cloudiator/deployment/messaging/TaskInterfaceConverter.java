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
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.TaskInterface;
import org.cloudiator.messages.entities.TaskEntities;

public class TaskInterfaceConverter implements
    TwoWayConverter<TaskEntities.TaskInterface, TaskInterface> {

  public static final TaskInterfaceConverter INSTANCE = new TaskInterfaceConverter();

  private static final LanceInterfaceConverter LANCE_INTERFACE_CONVERTER = LanceInterfaceConverter.INSTANCE;
  private static final FaasInterfaceConverter FAAS_INTERFACE_CONVERTER = FaasInterfaceConverter.INSTANCE;
  private static final SparkInterfaceConverter SPARK_INTERFACE_CONVERTER = SparkInterfaceConverter.INSTANCE;
  private static final DockerInterfaceConverter DOCKER_INTERFACE_CONVERTER = DockerInterfaceConverter.INSTANCE;

  private TaskInterfaceConverter() {

  }

  @Override
  public TaskEntities.TaskInterface applyBack(TaskInterface taskInterface) {

    if (taskInterface instanceof LanceInterface) {
      return TaskEntities.TaskInterface.newBuilder()
          .setLanceInterface(LANCE_INTERFACE_CONVERTER.applyBack(
              (LanceInterface) taskInterface)).build();
    } else if (taskInterface instanceof FaasInterface) {
      return TaskEntities.TaskInterface.newBuilder()
          .setFaasInterface(FAAS_INTERFACE_CONVERTER.applyBack(
              (FaasInterface) taskInterface)).build();
    } else if (taskInterface instanceof SparkInterface) {
      return TaskEntities.TaskInterface.newBuilder()
          .setSparkInterface(SPARK_INTERFACE_CONVERTER.applyBack(
              (SparkInterface) taskInterface)).build();
    } else if (taskInterface instanceof DockerInterface) {
      return TaskEntities.TaskInterface.newBuilder()
          .setDockerInterface(DOCKER_INTERFACE_CONVERTER.applyBack(
              (DockerInterface) taskInterface)).build();
    } else {
      throw new AssertionError(
          "Unknown type for task interface " + taskInterface.getClass().getName());
    }
  }

  @Override
  public TaskInterface apply(TaskEntities.TaskInterface taskInterface) {

    switch (taskInterface.getTaskInterfaceCase()) {
      case DOCKERINTERFACE:
        return DOCKER_INTERFACE_CONVERTER.apply(taskInterface.getDockerInterface());
      case LANCEINTERFACE:
        return LANCE_INTERFACE_CONVERTER.apply(taskInterface.getLanceInterface());
      case FAASINTERFACE:
        return FAAS_INTERFACE_CONVERTER.apply(taskInterface.getFaasInterface());
      case SPARKINTERFACE:
        return SPARK_INTERFACE_CONVERTER.apply(taskInterface.getSparkInterface());
      case TASKINTERFACE_NOT_SET:
        throw new AssertionError("TaskInterface is not set");
      default:
        throw new AssertionError(
            "Unknown TaskInterfaceCase " + taskInterface.getTaskInterfaceCase());
    }

  }
}
