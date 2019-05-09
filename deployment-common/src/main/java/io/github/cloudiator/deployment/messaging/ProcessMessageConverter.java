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

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcessBuilder;
import io.github.cloudiator.messaging.IpAddressMessageToIpAddress;
import java.util.stream.Collectors;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.NodeCluster;
import org.cloudiator.messages.entities.ProcessEntities.Process;
import org.cloudiator.messages.entities.ProcessEntities.Process.Builder;
import org.cloudiator.messages.entities.ProcessEntities.ProcessState;
import org.cloudiator.messages.entities.ProcessEntities.ProcessType;

public class ProcessMessageConverter implements
    TwoWayConverter<ProcessEntities.Process, CloudiatorProcess> {

  public static final ProcessMessageConverter INSTANCE = new ProcessMessageConverter();
  public static final ProcessStateConverter PROCESS_STATE_CONVERTER = ProcessStateConverter.INSTANCE;
  private static final IpAddressMessageToIpAddress IP_ADDRESS_CONVERTER = new IpAddressMessageToIpAddress();


  private ProcessMessageConverter() {

  }

  @Override
  public Process applyBack(CloudiatorProcess cloudiatorProcess) {

    if (cloudiatorProcess instanceof CloudiatorSingleProcess) {
      //Lance, Docker and FaaS processes

      final Builder builder = Process.newBuilder()
          .setNode(((CloudiatorSingleProcess) cloudiatorProcess).node());

      return finishBuilding(cloudiatorProcess, builder);


    } else if (cloudiatorProcess instanceof CloudiatorClusterProcess) {
      //Spark processes
      final Builder builder = Process.newBuilder()
          .setCluster(NodeCluster.newBuilder()
              .addAllNodes(((CloudiatorClusterProcess) cloudiatorProcess).nodes()).build());

      return finishBuilding(cloudiatorProcess, builder);

    } else {
      throw new IllegalStateException(
          "Unknown CloudiatorProcess interface: " + cloudiatorProcess.getClass().getName());
    }
  }

  private Process finishBuilding(CloudiatorProcess cloudiatorProcess, Builder builder) {
    builder.setId(cloudiatorProcess.id()).setUserId(cloudiatorProcess.userId())
        .setSchedule(cloudiatorProcess.scheduleId()).setTask(cloudiatorProcess.taskId())
        .setType(ProcessTypeConverter.INSTANCE.applyBack(cloudiatorProcess.type()))
        .setState(PROCESS_STATE_CONVERTER.applyBack(cloudiatorProcess.state()))
        .setTaskInterface(cloudiatorProcess.taskInterface())
        .addAllIpAddresses(
            cloudiatorProcess.ipAddresses().stream().map(IP_ADDRESS_CONVERTER::applyBack).collect(
                Collectors.toSet()))
    ;

    if (cloudiatorProcess.originId().isPresent()) {
      builder.setOriginId(cloudiatorProcess.originId().get());
    }

    if (cloudiatorProcess.reason().isPresent()) {
      builder.setReason(cloudiatorProcess.reason().get());
    }

    if (cloudiatorProcess.diagnostic().isPresent()) {
      builder.setDiagnostic(cloudiatorProcess.diagnostic().get());
    }

    if (cloudiatorProcess.endpoint().isPresent()) {
      builder.setEndpoint(cloudiatorProcess.endpoint().get());
    }

    return builder.build();
  }

  @Override
  public CloudiatorProcess apply(Process process) {

    switch (process.getRunsOnCase()) {
      case NODE:
        final CloudiatorSingleProcessBuilder cloudiatorSingleProcessBuilder = CloudiatorSingleProcessBuilder
            .create()
            .id(process.getId())
            .originId(process.getOriginId())
            .userId(process.getUserId())
            .scheduleId(process.getSchedule())
            .taskName(process.getTask())
            .taskInterface(process.getTaskInterface())
            .node(process.getNode())
            .state(PROCESS_STATE_CONVERTER.apply(process.getState()))
            .type(ProcessTypeConverter.INSTANCE.apply(process.getType()))
            .addAllIpAddresses(
                process.getIpAddressesList().stream().map(IP_ADDRESS_CONVERTER).collect(
                    Collectors.toSet()));

        if (!Strings.isNullOrEmpty(process.getOriginId())) {
          cloudiatorSingleProcessBuilder.originId(process.getOriginId());
        }

        if (!Strings.isNullOrEmpty(process.getDiagnostic())) {
          cloudiatorSingleProcessBuilder.diagnostic(process.getDiagnostic());
        }

        if (!Strings.isNullOrEmpty(process.getReason())) {
          cloudiatorSingleProcessBuilder.reason(process.getReason());
        }

        if (!Strings.isNullOrEmpty(process.getEndpoint())) {
          cloudiatorSingleProcessBuilder.endpoint(process.getEndpoint());
        }

        return cloudiatorSingleProcessBuilder.build();

      case CLUSTER:
        final CloudiatorClusterProcessBuilder cloudiatorClusterProcessBuilder = CloudiatorClusterProcessBuilder
            .create()
            .id(process.getId())
            .originId(process.getOriginId())
            .userId(process.getUserId())
            .scheduleId(process.getSchedule())
            .taskName(process.getTask())
            .taskInterface(process.getTaskInterface())
            .addAllNodes(process.getCluster().getNodesList())
            .state(PROCESS_STATE_CONVERTER.apply(process.getState()))
            .type(ProcessTypeConverter.INSTANCE.apply(process.getType()))
            .addAllIpAddresses(
                process.getIpAddressesList().stream().map(IP_ADDRESS_CONVERTER).collect(
                    Collectors.toSet()));

        if (!Strings.isNullOrEmpty(process.getOriginId())) {
          cloudiatorClusterProcessBuilder.originId(process.getOriginId());
        }

        if (!Strings.isNullOrEmpty(process.getDiagnostic())) {
          cloudiatorClusterProcessBuilder.diagnostic(process.getDiagnostic());
        }

        if (!Strings.isNullOrEmpty(process.getReason())) {
          cloudiatorClusterProcessBuilder.reason(process.getReason());
        }

        if (!Strings.isNullOrEmpty(process.getEndpoint())) {
          cloudiatorClusterProcessBuilder.endpoint(process.getEndpoint());
        }

        return cloudiatorClusterProcessBuilder.build();

      case RUNSON_NOT_SET:
        throw new IllegalStateException("RunsOn parameter not set for process: " + process.getId());

      default:
        throw new AssertionError("Unknown process: " + process);
    }


  }

  public static class ProcessStateConverter implements
      TwoWayConverter<ProcessEntities.ProcessState, CloudiatorProcess.ProcessState> {

    private static final ProcessStateConverter INSTANCE = new ProcessStateConverter();

    private ProcessStateConverter() {

    }

    @Override
    public ProcessState applyBack(CloudiatorProcess.ProcessState processState) {
      switch (processState) {
        case DELETED:
          return ProcessState.PROCESS_STATE_DELETED;
        case PENDING:
          return ProcessState.PROCESS_STATE_PENDING;
        case ERROR:
          return ProcessState.PROCESS_STATE_ERROR;
        case RUNNING:
          return ProcessState.PROCESS_STATE_RUNNING;
        case FINISHED:
          return ProcessState.PROCESS_STATE_FINISHED;
        default:
          throw new AssertionError("Unknown processState: " + processState);
      }
    }

    @Override
    public CloudiatorProcess.ProcessState apply(ProcessState processState) {
      switch (processState) {
        case PROCESS_STATE_ERROR:
          return CloudiatorProcess.ProcessState.ERROR;
        case PROCESS_STATE_PENDING:
          return CloudiatorProcess.ProcessState.PENDING;
        case PROCESS_STATE_DELETED:
          return CloudiatorProcess.ProcessState.DELETED;
        case PROCESS_STATE_RUNNING:
          return CloudiatorProcess.ProcessState.RUNNING;
        case PROCESS_STATE_FINISHED:
          return CloudiatorProcess.ProcessState.FINISHED;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown or illegal process state " + processState);
      }
    }
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
        case FAAS:
          return ProcessType.FAAS;
        case UNKNOWN:
          return ProcessType.UNKNOWN;
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
        case FAAS:
          return Type.FAAS;
        case UNKNOWN:
          return Type.UNKNOWN;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown process type: " + processType);
      }
    }
  }
}
