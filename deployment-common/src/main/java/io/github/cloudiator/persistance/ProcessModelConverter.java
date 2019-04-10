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
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcessBuilder;
import javax.annotation.Nullable;

class ProcessModelConverter implements OneWayConverter<ProcessModel, CloudiatorProcess> {

  static final ProcessModelConverter INSTANCE = new ProcessModelConverter();

  private ProcessModelConverter() {
  }

  @Nullable
  @Override
  public CloudiatorProcess apply(@Nullable ProcessModel processModel) {

    if (processModel == null) {
      return null;
    }

    if (processModel instanceof ProcessSingleModel) {
      return CloudiatorSingleProcessBuilder.create()
          .scheduleId(processModel.getSchedule().domainId())
          .type(processModel.getType())
          .id(processModel.getDomainId())
          .originId(processModel.getOriginId())
          .userId(processModel.getTenant().getUserId())
          .node(((ProcessSingleModel) processModel).getNode())
          .taskName(processModel.getTask())
          .taskInterface(processModel.getTaskInterface())
          .state(processModel.getState())
          .diagnostic(processModel.getDiagnostic())
          .reason(processModel.getReason())
          .build();
    } else if (processModel instanceof ProcessClusterModel) {

      return CloudiatorClusterProcessBuilder.create()
          .scheduleId(processModel.getSchedule().domainId())
          .type(processModel.getType())
          .id(processModel.getDomainId())
          .originId(processModel.getOriginId())
          .userId(processModel.getTenant().getUserId())
          .addAllNodes(((ProcessClusterModel) processModel).getNodes())
          .taskName(processModel.getTask())
          .taskInterface(processModel.getTaskInterface())
          .state(processModel.getState())
          .diagnostic(processModel.getDiagnostic())
          .reason(processModel.getReason())
          .build();
    } else {
      throw new AssertionError(
          "Illegal type of process model " + processModel.getClass().getSimpleName());
    }
  }


}
