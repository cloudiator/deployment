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

import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.Task;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class JobDomainRepository {

  private final JobModelRepository jobModelRepository;
  private final TaskDomainRepository taskDomainRepository;
  private final TenantModelRepository tenantModelRepository;
  private final PortDomainRepository portDomainRepository;
  private final CommunicationDomainRepository communicationDomainRepository;

  @Inject
  public JobDomainRepository(
      JobModelRepository jobModelRepository,
      TaskDomainRepository taskDomainRepository,
      TenantModelRepository tenantModelRepository,
      PortDomainRepository portDomainRepository,
      CommunicationDomainRepository communicationDomainRepository) {
    this.jobModelRepository = jobModelRepository;
    this.taskDomainRepository = taskDomainRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.portDomainRepository = portDomainRepository;
    this.communicationDomainRepository = communicationDomainRepository;
  }

  @Nullable
  JobModel getJobModelByName(String name) {
    return jobModelRepository.findByName(name);
  }


  private TenantModel getOrCreateTenantModel(String userId) {
    return tenantModelRepository.createOrGet(userId);
  }

  public void save(Job domain, String userId) {
    createModel(userId, domain);
  }

  private JobModel createModel(String userId, Job domain) {

    //create or get tenant
    final TenantModel tenantModel = getOrCreateTenantModel(userId);

    //create the job
    JobModel jobModel = new JobModel(domain.name(), tenantModel);
    jobModelRepository.save(jobModel);

    //create all tasks
    for (Task task : domain.tasks()) {
      final TaskModel taskModel = taskDomainRepository.saveAndGet(task, jobModel);

      for (Port port : task.ports()) {
        portDomainRepository.save(port, taskModel);
      }

    }

    //create all communications
    for (Communication communication : domain.communications()) {
      communicationDomainRepository.save(communication, jobModel);
    }

    return jobModel;

  }

}
