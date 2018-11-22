package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroups;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */
public class ProcessGroupConverter implements OneWayConverter<ProcessGroupModel, ProcessGroup> {

  private final ProcessModelConverter processModelConverter = ProcessModelConverter.INSTANCE;

  @Nullable
  @Override
  public ProcessGroup apply(@Nullable ProcessGroupModel processGroupModel) {
    if (processGroupModel == null) {
      return null;
    }

    return ProcessGroups.of(processGroupModel.getDomainId(),
        processGroupModel.getProcesses().stream().map(processModelConverter).collect(
            Collectors.toList()));
  }
}
