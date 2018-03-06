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
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.LanceInterfaceBuilder;
import io.github.cloudiator.deployment.domain.TaskInterface;
import javax.annotation.Nullable;

public class TaskInterfaceModelConverter implements
    OneWayConverter<TaskInterfaceModel, TaskInterface> {

  @Nullable
  @Override
  public TaskInterface apply(@Nullable TaskInterfaceModel taskInterfaceModel) {

    if (taskInterfaceModel == null) {
      return null;
    }

    if (taskInterfaceModel instanceof LanceTaskInterfaceModel) {
      return lanceInterface((LanceTaskInterfaceModel) taskInterfaceModel);
    } else {
      throw new AssertionError(
          String.format("taskInterfaceModel has illegal type %s.", taskInterfaceModel.getClass()));
    }
  }

  private LanceInterface lanceInterface(LanceTaskInterfaceModel lanceTaskInterfaceModel) {
    return LanceInterfaceBuilder.newBuilder().init(lanceTaskInterfaceModel.getInit())
        .install(lanceTaskInterfaceModel.getInstall()).postInstall(lanceTaskInterfaceModel.getPostInstall())
        .postStart(lanceTaskInterfaceModel.getPostStart()).postStop(lanceTaskInterfaceModel.getPostStop())
        .preInstall(lanceTaskInterfaceModel.getPreInstall()).preStart(lanceTaskInterfaceModel.getPreStart())
        .preStop(lanceTaskInterfaceModel.getPreStop()).shutdown(lanceTaskInterfaceModel.getShutdown())
        .start(lanceTaskInterfaceModel.getStart())
        .startDetection(lanceTaskInterfaceModel.getStartDetection()).stop(lanceTaskInterfaceModel.getStop())
        .stopDetection(lanceTaskInterfaceModel.getStopDetection()).build();
  }
}
