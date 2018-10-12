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

import io.github.cloudiator.deployment.domain.CloudiatorProcess;

class ProcessDomainRepository {

  private final ProcessModelRepository processModelRepository;

  ProcessDomainRepository(
      ProcessModelRepository processModelRepository) {
    this.processModelRepository = processModelRepository;
  }


  void save(CloudiatorProcess domain, ScheduleModel scheduleModel) {
    saveAndGet(domain, scheduleModel);
  }

  ProcessModel saveAndGet(CloudiatorProcess domain, ScheduleModel scheduleModel) {
    return createProcessModel(domain, scheduleModel);
  }

  private ProcessModel createProcessModel(CloudiatorProcess domain, ScheduleModel scheduleModel) {

    final ProcessModel processModel = new ProcessModel(domain.id(), scheduleModel, domain.taskId(),
        domain.nodeId(), domain.state());
    processModelRepository.save(processModel);

    return processModel;
  }

}
