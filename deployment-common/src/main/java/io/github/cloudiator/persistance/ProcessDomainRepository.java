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
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ProcessDomainRepository {

  private final ProcessModelRepository processModelRepository;
  private final ScheduleModelRepository scheduleModelRepository;
  private static final ProcessModelConverter PROCESS_MODEL_CONVERTER = ProcessModelConverter.INSTANCE;
  private final IpAddressDomainRepository ipAddressDomainRepository;
  private final IpGroupModelRepository ipGroupModelRepository;

  @Inject
  ProcessDomainRepository(
      ProcessModelRepository processModelRepository,
      ScheduleModelRepository scheduleModelRepository,
      IpAddressDomainRepository ipAddressDomainRepository,
      IpGroupModelRepository ipGroupModelRepository) {
    this.processModelRepository = processModelRepository;
    this.scheduleModelRepository = scheduleModelRepository;
    this.ipAddressDomainRepository = ipAddressDomainRepository;
    this.ipGroupModelRepository = ipGroupModelRepository;
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

  public List<CloudiatorProcess> getAll() {
    return processModelRepository.findAll().stream().map(PROCESS_MODEL_CONVERTER).collect(
        Collectors.toList());
  }

  public CloudiatorProcess save(CloudiatorProcess domain) {
    checkNotNull(domain, "domain is null");
    return PROCESS_MODEL_CONVERTER.apply(saveAndGet(domain));
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
    processModel.setOriginId(domain.originId().orElse(null));
    processModel.setDiagnostic(domain.diagnostic().orElse(null));
    processModel.setType(domain.type());
    processModel.setIpGroupModel(generateIpModel(domain));
    processModel.setEndpoint(domain.endpoint().orElse(null));
    processModel.setStart(processModel.getStart());
    processModel.setStop(processModel.getStop());

    if (processModel instanceof ProcessClusterModel) {
      ((ProcessClusterModel) processModel).setNodes(domain.nodes());
    }

    return processModel;

  }

  @Nullable
  private IpGroupModel generateIpModel(CloudiatorProcess domain) {
    IpGroupModel ipGroupModel = null;
    if (!domain.ipAddresses().isEmpty()) {
      ipGroupModel = ipAddressDomainRepository.saveAndGet(domain.ipAddresses());
      ipGroupModelRepository.save(ipGroupModel);
    }
    return ipGroupModel;
  }


  private ProcessModel createProcessModel(CloudiatorProcess domain) {

    //TODO: fetch this by the message!?

    //get the schedule model
    final ScheduleModel scheduleModel = getScheduleModel(domain.scheduleId(), domain.userId());

    if (domain instanceof CloudiatorSingleProcess) {

      return new ProcessSingleModel(domain.id(), domain.originId().orElse(null), scheduleModel,
          domain.taskId(), domain.taskInterface(), domain.state(),
          domain.type(), ((CloudiatorSingleProcess) domain).node(),
          domain.diagnostic().orElse(null), domain.reason().orElse(null),
          domain.endpoint().orElse(null), generateIpModel(domain), domain.start(),
          domain.stop().orElse(null));

    } else if (domain instanceof CloudiatorClusterProcess) {

      return new ProcessClusterModel(domain.id(), domain.originId().orElse(null), scheduleModel,
          domain.taskId(), domain.taskInterface(), domain.state(),
          domain.type(), domain.nodes(),
          domain.diagnostic().orElse(null), domain.reason().orElse(null),
          domain.endpoint().orElse(null), generateIpModel(domain), domain.start(),
          domain.stop().orElse(null));

    } else {
      throw new AssertionError(
          "Unknown CloudiatorProcess interface for persisting CloudiatorProcess: " + domain
              .getClass().getName());
    }


  }
}
