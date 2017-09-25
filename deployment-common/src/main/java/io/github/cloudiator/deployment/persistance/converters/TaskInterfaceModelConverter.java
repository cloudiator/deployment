package io.github.cloudiator.deployment.persistance.converters;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.LanceInterfaceBuilder;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.persistance.entities.LanceInterfaceModel;
import io.github.cloudiator.deployment.persistance.entities.TaskInterfaceModel;
import javax.annotation.Nullable;

public class TaskInterfaceModelConverter implements
    OneWayConverter<TaskInterfaceModel, TaskInterface> {

  @Nullable
  @Override
  public TaskInterface apply(@Nullable TaskInterfaceModel taskInterfaceModel) {

    if (taskInterfaceModel == null) {
      return null;
    }

    if (taskInterfaceModel instanceof LanceInterfaceModel) {
      return lanceInterface((LanceInterfaceModel) taskInterfaceModel);
    } else {
      throw new AssertionError(
          String.format("taskInterfaceModel has illegal type %s.", taskInterfaceModel.getClass()));
    }
  }

  private LanceInterface lanceInterface(LanceInterfaceModel lanceInterfaceModel) {
    return LanceInterfaceBuilder.newBuilder().init(lanceInterfaceModel.getInit())
        .install(lanceInterfaceModel.getInstall()).postInstall(lanceInterfaceModel.getPostInstall())
        .postStart(lanceInterfaceModel.getPostStart()).postStop(lanceInterfaceModel.getPostStop())
        .preInstall(lanceInterfaceModel.getPreInstall()).preStart(lanceInterfaceModel.getPreStart())
        .preStop(lanceInterfaceModel.getPreStop()).shutdown(lanceInterfaceModel.getShutdown())
        .start(lanceInterfaceModel.getStart())
        .startDetection(lanceInterfaceModel.getStartDetection()).stop(lanceInterfaceModel.getStop())
        .stopDetection(lanceInterfaceModel.getStopDetection()).build();
  }
}
