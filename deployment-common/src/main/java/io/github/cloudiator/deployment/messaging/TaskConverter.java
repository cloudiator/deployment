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
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.ServiceBehaviour;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import org.cloudiator.matchmaking.converters.OptimizationConverter;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.Task.Builder;

public class TaskConverter implements TwoWayConverter<TaskEntities.Task, Task> {

  private static final PortConverter PORT_CONVERTER = new PortConverter();
  private static final TaskInterfaceConverter TASK_INTERFACE_CONVERTER = TaskInterfaceConverter.INSTANCE;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final OptimizationConverter OPTIMIZATION_CONVERTER = OptimizationConverter.INSTANCE;
  private static final ServiceBehaviourConverter SERVICE_BEHAVIOUR_CONVERTER = ServiceBehaviourConverter.INSTANCE;
  private static final PeriodicBehaviourConverter PERIODIC_BEHAVIOUR_CONVERTER = PeriodicBehaviourConverter.INSTANCE;

  @Override
  public TaskEntities.Task applyBack(Task task) {

    Builder builder = TaskEntities.Task.newBuilder();
    builder.setName(task.name());

    task.ports().stream().map(PORT_CONVERTER::applyBack).forEach(builder::addPorts);
    task.interfaces().stream().map(TASK_INTERFACE_CONVERTER::applyBack)
        .forEach(builder::addInterfaces);

    task.requirements().stream().map(REQUIREMENT_CONVERTER::applyBack)
        .forEach(builder::addRequirements);

    if (task.optimization().isPresent()) {
      builder.setOptimization(OPTIMIZATION_CONVERTER.applyBack(task.optimization().get()));
    }

    if (task.behaviour() instanceof PeriodicBehaviour) {
      builder.setPeriodic(PERIODIC_BEHAVIOUR_CONVERTER.applyBack(
          (PeriodicBehaviour) task.behaviour()));
    } else if (task.behaviour() instanceof ServiceBehaviour) {
      builder
          .setService(SERVICE_BEHAVIOUR_CONVERTER.applyBack((ServiceBehaviour) task.behaviour()));
    } else {
      throw new AssertionError(
          "Unknown serivce behaviour type " + task.behaviour().getClass().getName());
    }

    return builder.build();
  }

  @Override
  public Task apply(TaskEntities.Task task) {

    final TaskBuilder taskBuilder = TaskBuilder.newBuilder().name(task.getName());

    task.getPortsList().forEach(port -> taskBuilder.addPort(PORT_CONVERTER.apply(port)));
    task.getInterfacesList().stream().map(TASK_INTERFACE_CONVERTER)
        .forEach(taskBuilder::addInterface);
    if (task.hasOptimization()) {
      taskBuilder.optimization(OPTIMIZATION_CONVERTER.apply(task.getOptimization()));
    }
    task.getRequirementsList().stream().map(REQUIREMENT_CONVERTER)
        .forEach(taskBuilder::addRequirement);

    switch (task.getBehaviourCase()) {
      case PERIODIC:
        taskBuilder.behaviour(PERIODIC_BEHAVIOUR_CONVERTER.apply(task.getPeriodic()));
        break;
      case SERVICE:
        taskBuilder.behaviour(SERVICE_BEHAVIOUR_CONVERTER.apply(task.getService()));
        break;
      case BEHAVIOUR_NOT_SET:
      default:
        throw new AssertionError("Unknown behaviour case " + task.getBehaviourCase());

    }

    return taskBuilder.build();
  }
}
