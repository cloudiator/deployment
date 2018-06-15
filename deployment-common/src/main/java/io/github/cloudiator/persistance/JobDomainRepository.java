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
import io.github.cloudiator.deployment.domain.Task;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class JobDomainRepository {

  private final JobModelRepository jobModelRepository;
  private final TaskDomainRepository taskDomainRepository;
  private final TenantModelRepository tenantModelRepository;

  private final CommunicationDomainRepository communicationDomainRepository;
  private static final JobModelConverter JOB_MODEL_CONVERTER = JobModelConverter.INSTANCE;

  @Inject
  public JobDomainRepository(
      JobModelRepository jobModelRepository,
      TaskDomainRepository taskDomainRepository,
      TenantModelRepository tenantModelRepository,
      CommunicationDomainRepository communicationDomainRepository) {
    this.jobModelRepository = jobModelRepository;
    this.taskDomainRepository = taskDomainRepository;
    this.tenantModelRepository = tenantModelRepository;
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

  public List<Job> findByUserId(String userId) {

    return jobModelRepository.findByUser(userId).stream().map(JOB_MODEL_CONVERTER)
        .collect(Collectors.toList());
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

      final List<PortRequiredModel> requiredPorts = taskModel.getRequiredPorts();
      final List<PortProvidedModel> providedPorts = taskModel.getProvidedPorts();

      boolean test = true;

    }

    //create all communications
    for (Communication communication : domain.communications()) {
      communicationDomainRepository.save(communication, jobModel);
    }

    return jobModel;

  }

}
