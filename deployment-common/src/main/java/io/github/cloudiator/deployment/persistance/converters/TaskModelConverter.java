package io.github.cloudiator.deployment.persistance.converters;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import io.github.cloudiator.deployment.persistance.entities.TaskModel;
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
