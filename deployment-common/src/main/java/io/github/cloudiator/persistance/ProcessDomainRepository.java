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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessDomainRepository {

  private final ProcessModelRepository processModelRepository;
  private final ScheduleModelRepository scheduleModelRepository;
  private static final ProcessModelConverter PROCESS_MODEL_CONVERTER = ProcessModelConverter.INSTANCE;
  private static  final NodeGroupConverter NODE_GROUP_CONVERTER = new NodeGroupConverter();
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


  public void save(ProcessGroup processGroup, String userId) {

    checkNotNull(processGroup, "domain is null");
    checkNotNull(userId, "userId is null");
    checkArgument(!userId.isEmpty(), "userId is empty");


    //persist processes
    for(CloudiatorProcess cloudiatorProcess : processGroup.cloudiatorProcesses()){

      final ScheduleModel scheduleModel = scheduleModelRepository
          .findByIdAndUser(cloudiatorProcess.scheduleId(), userId);
      if (scheduleModel == null) {
        throw new IllegalStateException(
            String.format("Schedule with id %s does not exist.", cloudiatorProcess.scheduleId()));
      }
      //persist processGroup
      final ProcessGroupModel processGroupModel = new ProcessGroupModel(processGroup.id(), scheduleModel);
      processGroupModelRepository.save(processGroupModel);

      final ProcessModel processModel = saveAndGet(cloudiatorProcess,scheduleModel, processGroupModel);
      processGroupModel.addProcess(processModel);
      processModel.assignGroup(processGroupModel);
      processModelRepository.save(processModel);

      processGroupModelRepository.save(processGroupModel);
    }





  }

  public void delete(String processId, String userId) {
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


  ProcessModel saveAndGet(CloudiatorProcess domain, ScheduleModel scheduleModel, ProcessGroupModel processGroupModel) {
    return createProcessModel(domain, scheduleModel, processGroupModel);
  }

  private ProcessModel createProcessModel(CloudiatorProcess domain, ScheduleModel scheduleModel, ProcessGroupModel processGroupModel) {

    //TODO: fetch this by the message!?


    if(domain instanceof CloudiatorSingleProcess){

      final ProcessModel processModel = new ProcessModel(domain.id(), scheduleModel, domain.taskId(),
          ((CloudiatorSingleProcess) domain).node(),
          null, domain.state(), domain.type(), processGroupModel);
      processModelRepository.save(processModel);

      return processModel;

    }else if(domain instanceof CloudiatorClusterProcess){

      final ProcessModel processModel = new ProcessModel(domain.id(), scheduleModel, domain.taskId(), null,
          ((CloudiatorClusterProcess) domain).nodeGroup(), domain.state(), domain.type(), processGroupModel);
      processModelRepository.save(processModel);

      return processModel;
    }else{
      throw  new IllegalStateException("Unknown CloudiatorProcess interface for persisting CloudiatorProcess: " + domain.getClass().getName());
    }


  }

}
