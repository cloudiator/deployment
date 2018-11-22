package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroups;
import java.util.LinkedList;
import java.util.List;
import org.cloudiator.messages.entities.ProcessEntities;
import org.cloudiator.messages.entities.ProcessEntities.ProcessGroup.Builder;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */
public class ProcessGroupMessageConverter implements
    TwoWayConverter<ProcessEntities.ProcessGroup, ProcessGroup> {

  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER =  ProcessMessageConverter.INSTANCE;

  @Override
  public ProcessEntities.ProcessGroup applyBack(ProcessGroup processGroup) {
    final Builder builder = ProcessEntities.ProcessGroup.newBuilder();
    for (final CloudiatorProcess cloudiatorProcess : processGroup.cloudiatorProcesses()) {
      builder.addProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess));
    }
    builder.setId(processGroup.id());

    return builder.build();
  }

  @Override
  public ProcessGroup apply(ProcessEntities.ProcessGroup processGroup) {

    List<CloudiatorProcess> processList = new LinkedList<>();
    for(ProcessEntities.Process process : processGroup.getProcessList()){
      processList.add(PROCESS_MESSAGE_CONVERTER.apply(process));
    }

    return ProcessGroups.of(processGroup.getId(), processList);
  }
}
