package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroupBuilder;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.ProcessGroup.Builder;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */
public class ProcessGroupMessageConverter implements
    TwoWayConverter<ProcessEntities.ProcessGroup, ProcessGroup> {

  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;

  @Override
  public ProcessEntities.ProcessGroup applyBack(ProcessGroup processGroup) {
    final Builder builder = ProcessEntities.ProcessGroup.newBuilder();
    for (final CloudiatorProcess cloudiatorProcess : processGroup.cloudiatorProcesses()) {
      builder.addProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess));
    }
    builder.setId(processGroup.id());
    builder.setUserId(processGroup.userId());
    builder.setScheduleId(processGroup.scheduleId());

    return builder.build();
  }

  @Override
  public ProcessGroup apply(ProcessEntities.ProcessGroup processGroup) {

    final ProcessGroupBuilder processGroupBuilder = ProcessGroupBuilder.create();
    processGroupBuilder.id(processGroup.getId());
    processGroupBuilder.userId(processGroup.getUserId());
    processGroupBuilder.scheduleId(processGroup.getScheduleId());

    for (ProcessEntities.Process process : processGroup.getProcessList()) {
      processGroupBuilder.addProcess(PROCESS_MESSAGE_CONVERTER.apply(process));
    }

    return processGroupBuilder.build();
  }
}
