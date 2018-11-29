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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorProcessBuilder;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.Process;
import org.cloudiator.messages.entities.ProcessEntities.ProcessType;

public class ProcessMessageConverter implements
    TwoWayConverter<ProcessEntities.Process, CloudiatorProcess> {

  public static final ProcessMessageConverter INSTANCE = new ProcessMessageConverter();


  private ProcessMessageConverter() {

  }

  @Override
  public Process applyBack(CloudiatorProcess cloudiatorProcess) {

    return Process.newBuilder().setId(cloudiatorProcess.id())
        .setSchedule(cloudiatorProcess.scheduleId())
        .setNodeGroup(cloudiatorProcess.nodeGroup())
        .setTask(cloudiatorProcess.taskId())
        .setType(ProcessTypeConverter.INSTANCE.applyBack(cloudiatorProcess.type())).build();
  }

  @Override
  public CloudiatorProcess apply(Process process) {


    return CloudiatorProcessBuilder.newBuilder()
        .id(process.getId())
        .scheduleId(process.getSchedule())
        .taskName(process.getTask())
        .nodeGroup(process.getNodeGroup())
        .type(ProcessTypeConverter.INSTANCE.apply(process.getType())).build();
  }

  private static class ProcessTypeConverter implements
      TwoWayConverter<ProcessEntities.ProcessType, CloudiatorProcess.Type> {

    private static final ProcessTypeConverter INSTANCE = new ProcessTypeConverter();

    @Override
    public ProcessType applyBack(Type type) {
      switch (type) {
        case LANCE:
          return ProcessType.LANCE;
        case SPARK:
          return ProcessType.SPARK;
        default:
          throw new AssertionError("Unknown type: " + type);
      }
    }

    @Override
    public Type apply(ProcessType processType) {
      switch (processType) {
        case SPARK:
          return Type.SPARK;
        case LANCE:
          return Type.LANCE;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown process type: " + processType);
      }
    }
  }
}
