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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import org.cloudiator.matchmaking.domain.Requirement;

class TaskDomainRepository {

  private final TaskModelRepository taskModelRepository;
  private final PortDomainRepository portDomainRepository;
  private final TaskInterfaceDomainRepository taskInterfaceDomainRepository;
  private final RequirementDomainRepository requirementDomainRepository;
  private final OptimizationDomainRepository optimizationDomainRepository;
  private final BehaviourDomainRepository behaviourDomainRepository;

  @Inject
  public TaskDomainRepository(
      TaskModelRepository taskModelRepository,
      PortDomainRepository portDomainRepository,
      TaskInterfaceDomainRepository taskInterfaceDomainRepository,
      RequirementDomainRepository requirementDomainRepository,
      OptimizationDomainRepository optimizationDomainRepository,
      BehaviourDomainRepository behaviourDomainRepository) {
    this.taskModelRepository = taskModelRepository;
    this.portDomainRepository = portDomainRepository;
    this.taskInterfaceDomainRepository = taskInterfaceDomainRepository;
    this.requirementDomainRepository = requirementDomainRepository;
    this.optimizationDomainRepository = optimizationDomainRepository;
    this.behaviourDomainRepository = behaviourDomainRepository;
  }

  void save(Task domain, JobModel jobModel) {
    saveAndGet(domain, jobModel);
  }

  TaskModel saveAndGet(Task domain, JobModel jobModel) {
    final TaskModel taskModel = createTaskModel(domain, jobModel);
    taskModelRepository.save(taskModel);
    return taskModel;
  }

  private TaskModel createTaskModel(Task domain, JobModel jobModel) {

    OptimizationModel optimizationModel = null;
    if (domain.optimization().isPresent()) {
      optimizationModel = optimizationDomainRepository
          .saveAndGet(domain.optimization().get());
    }

    BehaviourModel behaviourModel = behaviourDomainRepository.saveAndGet(domain.behaviour());

    final TaskModel taskModel = new TaskModel(domain.name(), jobModel, optimizationModel,
        behaviourModel);
    taskModelRepository.save(taskModel);

    for (Port port : domain.ports()) {
      portDomainRepository.save(port, taskModel);
    }

    for (TaskInterface taskInterface : domain.interfaces()) {
      final TaskInterfaceModel taskInterfaceModel = taskInterfaceDomainRepository
          .saveAndGet(taskInterface, taskModel);
    }

    for (Requirement requirement : domain.requirements()) {
      requirementDomainRepository.saveAndGet(requirement, taskModel, null);
    }

    return taskModel;
  }

}
