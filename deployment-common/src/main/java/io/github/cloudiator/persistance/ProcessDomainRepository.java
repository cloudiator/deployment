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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessDomainRepository {

  private final ProcessModelRepository processModelRepository;
  private final ScheduleModelRepository scheduleModelRepository;
  private static final ProcessModelConverter PROCESS_MODEL_CONVERTER = ProcessModelConverter.INSTANCE;
  private static final ProcessGroupConverter PROCESS_GROUP_CONVERTER = new ProcessGroupConverter();
  private final ProcessGroupModelRepository processGroupModelRepository;

  @Inject
  ProcessDomainRepository(
      ProcessModelRepository processModelRepository,
      ScheduleModelRepository scheduleModelRepository,
      ProcessGroupModelRepository processGroupModelRepository) {
    this.processModelRepository = processModelRepository;
    this.scheduleModelRepository = scheduleModelRepository;
    this.processGroupModelRepository = processGroupModelRepository;

  }

  public void save(ProcessGroup processGroup) {
    checkNotNull(processGroup, "processGroup is null");

    final ScheduleModel scheduleModel = getScheduleModel(processGroup.scheduleId(),
        processGroup.userId());

    final ProcessGroupModel processGroupModel = new ProcessGroupModel(processGroup.id(),
        scheduleModel);
    processGroupModelRepository.save(processGroupModel);

    for (CloudiatorProcess cloudiatorProcess : processGroup.cloudiatorProcesses()) {
      final ProcessModel processModel = saveAndGet(cloudiatorProcess);
      processGroupModel.addProcess(processModel);
      processModel.assignGroup(processGroupModel);
      processModelRepository.save(processModel);
    }

    processGroupModelRepository.save(processGroupModel);
  }

  public void delete(String processId, String userId) {

    //todo: check if we can delete an empty group model

    final ProcessModel processModel = processModelRepository.findByIdAndUser(processId, userId);
    if (processModel == null) {
      throw new IllegalStateException(String
          .format("Process model with the id %s for the user %s does not exist.", processId,
              userId));
    }
    processModelRepository.delete(processModel);
  }

  public CloudiatorProcess getByIdAndUser(String id, String userId) {
    return PROCESS_MODEL_CONVERTER.apply(processModelRepository.findByIdAndUser(id, userId));
  }

  public List<CloudiatorProcess> getByScheduleIdAndUser(String scheduleId, String userId) {

    return processModelRepository.findByScheduleAndUser(scheduleId, userId).stream()
        .map(PROCESS_MODEL_CONVERTER).collect(
            Collectors.toList());
  }

  public List<CloudiatorProcess> getByUser(String userId) {
    return processModelRepository.findByUser(userId).stream().map(PROCESS_MODEL_CONVERTER).collect(
        Collectors.toList());
  }

  /*
  void save(CloudiatorProcess domain, ScheduleModel scheduleModel, ProcessGroupModel processGroupModel ) {
    saveAndGet(domain, scheduleModel, processGroupModel);
  }
  */

  public void save(CloudiatorProcess domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }


  ProcessModel saveAndGet(CloudiatorProcess domain) {

    //check if exists
    ProcessModel processModel = processModelRepository
        .findByIdAndUser(domain.id(), domain.userId());

    if (processModel == null) {
      processModel = createProcessModel(domain);
    } else {
      processModel = updateProcessModel(domain, processModel);
    }

    processModelRepository.save(processModel);

    return processModel;
  }

  private ScheduleModel getScheduleModel(String scheduleId, String userId) {
    final ScheduleModel scheduleModel = scheduleModelRepository
        .findByIdAndUser(scheduleId, userId);
    if (scheduleModel == null) {
      throw new IllegalStateException(
          String.format("Schedule with id %s does not exist for user %s.", scheduleId, userId));
    }
    return scheduleModel;
  }

  private ProcessModel updateProcessModel(CloudiatorProcess domain, ProcessModel processModel) {

    checkState(processModel.getDomainId().equals(domain.id()), "domain id is not equal");

    processModel.setState(domain.state());

    return processModel;

  }

  private ProcessModel createProcessModel(CloudiatorProcess domain) {

    //TODO: fetch this by the message!?

    //get the schedule model
    final ScheduleModel scheduleModel = getScheduleModel(domain.scheduleId(), domain.userId());

    if (domain instanceof CloudiatorSingleProcess) {

      return new ProcessSingleModel(domain.id(), scheduleModel, domain.taskId(), domain.state(),
          domain.type(), null, ((CloudiatorSingleProcess) domain).node());

    } else if (domain instanceof CloudiatorClusterProcess) {

      return new ProcessClusterModel(domain.id(), scheduleModel, domain.taskId(), domain.state(),
          domain.type(), null, new ArrayList<>(((CloudiatorClusterProcess) domain).nodes()));

    } else {
      throw new AssertionError(
          "Unknown CloudiatorProcess interface for persisting CloudiatorProcess: " + domain
              .getClass().getName());
    }


  }

  public List<ProcessGroup> findGroupsByTenant(String userId) {
    return processGroupModelRepository.findByTenant(userId).stream().map(PROCESS_GROUP_CONVERTER)
        .collect(Collectors.toList());
  }

  public ProcessGroup findGroupByTenantAndId(String userId, String processGroupId) {
    return PROCESS_GROUP_CONVERTER
        .apply(processGroupModelRepository.findByTenantAndDomainId(userId, processGroupId));
  }


}
