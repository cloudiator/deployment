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
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.DockerInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.DockerInterface.Builder;

public class DockerInterfaceConverter implements
    TwoWayConverter<TaskEntities.DockerInterface, DockerInterface> {

  public static final DockerInterfaceConverter INSTANCE = new DockerInterfaceConverter();

  @Override
  public TaskEntities.DockerInterface applyBack(DockerInterface dockerInterface) {
    final Builder builder = TaskEntities.DockerInterface.newBuilder()
        .setDockerImage(dockerInterface.dockerImage())
        .putAllEnvironment(dockerInterface.environment());

    if (dockerInterface.portUpdateAction().isPresent()) {
      builder.setPortUpdateAction(dockerInterface.portUpdateAction().get());
    }

    return builder.build();
  }

  @Override
  public DockerInterface apply(TaskEntities.DockerInterface dockerInterface) {

    return DockerInterfaceBuilder.newBuilder().dockerImage(dockerInterface.getDockerImage())
        .environment(dockerInterface.getEnvironmentMap())
        .portUpdateAction(dockerInterface.getPortUpdateAction()).build();
  }
}
