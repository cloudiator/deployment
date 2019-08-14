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
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.matchmaking.domain.Requirement;

public class JobDomainRepository {

  private final JobModelRepository jobModelRepository;
  private final TaskDomainRepository taskDomainRepository;
  private final TenantModelRepository tenantModelRepository;
  private final RequirementDomainRepository requirementDomainRepository;
  private final OptimizationDomainRepository optimizationDomainRepository;

  private final CommunicationDomainRepository communicationDomainRepository;
  private static final JobModelConverter JOB_MODEL_CONVERTER = JobModelConverter.INSTANCE;

  @Inject
  public JobDomainRepository(
      JobModelRepository jobModelRepository,
      TaskDomainRepository taskDomainRepository,
      TenantModelRepository tenantModelRepository,
      RequirementDomainRepository requirementDomainRepository,
      OptimizationDomainRepository optimizationDomainRepository,
      CommunicationDomainRepository communicationDomainRepository) {
    this.jobModelRepository = jobModelRepository;
    this.taskDomainRepository = taskDomainRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.requirementDomainRepository = requirementDomainRepository;
    this.optimizationDomainRepository = optimizationDomainRepository;
    this.communicationDomainRepository = communicationDomainRepository;
  }


  private TenantModel getOrCreateTenantModel(String userId) {
    return tenantModelRepository.createOrGet(userId);
  }

  public void save(Job domain) {
    createModel(domain);
  }

  public List<Job> findByUserId(String userId) {

    return jobModelRepository.findByUser(userId).stream().map(JOB_MODEL_CONVERTER)
        .collect(Collectors.toList());
  }

  public Optional<Job> findByUserAndId(String userId, String jobId) {
    final JobModel byNameAndUser = jobModelRepository.findByUUIDAndUser(jobId, userId);
    return Optional.ofNullable(JOB_MODEL_CONVERTER.apply(byNameAndUser));
  }

  private JobModel createModel(Job domain) {

    //create or get tenant
    final TenantModel tenantModel = getOrCreateTenantModel(domain.userId());

    //create the job
    JobModel jobModel = new JobModel(domain.id(), domain.name(), tenantModel);
    jobModelRepository.save(jobModel);

    //create all tasks
    for (Task task : domain.tasks()) {
      final TaskModel taskModel = taskDomainRepository.saveAndGet(task, jobModel);
    }

    //create all communications
    for (Communication communication : domain.communications()) {
      communicationDomainRepository.save(communication, jobModel);
    }

    //create optimization model
    OptimizationModel optimizationModel = null;
    if (domain.optimization().isPresent()) {
      optimizationModel = optimizationDomainRepository
          .saveAndGet(domain.optimization().get());
    }

    //create all requirements
    for (Requirement requirement : domain.requirements()) {
      requirementDomainRepository.saveAndGet(requirement, null, jobModel);
    }

    return jobModel;

  }

}
