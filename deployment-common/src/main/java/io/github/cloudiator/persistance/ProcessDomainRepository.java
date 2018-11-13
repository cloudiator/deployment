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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessDomainRepository {

  private final ProcessModelRepository processModelRepository;
  private final ScheduleModelRepository scheduleModelRepository;
  private static final ProcessModelConverter PROCESS_MODEL_CONVERTER = ProcessModelConverter.INSTANCE;

  @Inject
  ProcessDomainRepository(
      ProcessModelRepository processModelRepository,
      ScheduleModelRepository scheduleModelRepository) {
    this.processModelRepository = processModelRepository;
    this.scheduleModelRepository = scheduleModelRepository;
  }

  public void save(CloudiatorProcess domain, String userId) {

    checkNotNull(domain, "domain is null");
    checkNotNull(userId, "userId is null");
    checkArgument(!userId.isEmpty(), "userId is empty");

    final ScheduleModel scheduleModel = scheduleModelRepository
        .findByIdAndUser(domain.scheduleId(), userId);
    if (scheduleModel == null) {
      throw new IllegalStateException(
          String.format("Schedule with id %s does not exist.", domain.scheduleId()));
    }

    save(domain, scheduleModel);
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

  void save(CloudiatorProcess domain, ScheduleModel scheduleModel) {
    saveAndGet(domain, scheduleModel);
  }

  ProcessModel saveAndGet(CloudiatorProcess domain, ScheduleModel scheduleModel) {
    return createProcessModel(domain, scheduleModel);
  }

  private ProcessModel createProcessModel(CloudiatorProcess domain, ScheduleModel scheduleModel) {

    final ProcessModel processModel = new ProcessModel(domain.id(), scheduleModel, domain.taskId(),
        domain.nodeId(), domain.state(), domain.type());
    processModelRepository.save(processModel);

    return processModel;
  }

}
