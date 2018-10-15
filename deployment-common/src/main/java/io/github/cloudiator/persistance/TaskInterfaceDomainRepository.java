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
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.domain.Trigger;

public class TaskInterfaceDomainRepository {

  private final TaskInterfaceModelRepository taskInterfaceModelRepository;
  private final TriggerDomainRepository triggerDomainRepository;

  @Inject
  public TaskInterfaceDomainRepository(
      TaskInterfaceModelRepository taskInterfaceModelRepository,
      TriggerDomainRepository triggerDomainRepository) {
    this.taskInterfaceModelRepository = taskInterfaceModelRepository;
    this.triggerDomainRepository = triggerDomainRepository;
  }

  TaskInterfaceModel saveAndGet(TaskInterface domain, TaskModel taskModel) {

    final TaskInterfaceModel taskInterfaceModel = createTaskInterfaceModel(domain, taskModel);
    taskInterfaceModelRepository.save(taskInterfaceModel);
    return taskInterfaceModel;

  }

  private TaskInterfaceModel createTaskInterfaceModel(TaskInterface domain, TaskModel taskModel) {

    if (domain instanceof LanceInterface) {
      return createLanceInterfaceModel((LanceInterface) domain, taskModel);
    } else if (domain instanceof FaasInterface) {
      return createFaasInterfaceModel((FaasInterface) domain, taskModel);
    } else if (domain instanceof SparkInterface) {
      return createSparkInterfaceModel((SparkInterface) domain, taskModel);
    } else {
      throw new AssertionError("TaskInterface is of unknown type " + domain.getClass().getName());
    }
  }

  private LanceTaskInterfaceModel createLanceInterfaceModel(LanceInterface domain,
      TaskModel taskModel) {

    return new LanceTaskInterfaceModel(taskModel, domain.containerType(),
        domain.init().orElse(null),
        domain.preInstall().orElse(null), domain.install().orElse(null),
        domain.postInstall().orElse(null), domain.preStart().orElse(null),
        domain.start(), domain.startDetection().orElse(null), domain.stopDetection().orElse(null),
        domain.postStart().orElse(null),
        domain.preStop().orElse(null), domain.stop().orElse(null), domain.postStop().orElse(null),
        domain.shutdown().orElse(null));

  }

  private TaskInterfaceModel createFaasInterfaceModel(
      FaasInterface domain, TaskModel taskModel) {
    FaasTaskInterfaceModel faasInterfaceModel = new FaasTaskInterfaceModel(taskModel,
        domain.functionName(),
        domain.sourceCodeUrl(),
        domain.handler(),
        domain.runtime(),
        domain.timeout(),
        domain.memory());

    taskInterfaceModelRepository.save(faasInterfaceModel);
    for (Trigger trigger : domain.triggers()) {
      triggerDomainRepository.saveAndGet(trigger, faasInterfaceModel);
    }
    return faasInterfaceModel;
  }

  private SparkTaskInterfaceModel createSparkInterfaceModel(SparkInterface domain,
      TaskModel taskModel) {
    return new SparkTaskInterfaceModel(taskModel, domain.file(), domain.className().orElse(null),
        domain.arguments(), domain.sparkArguments(), domain.sparkConfiguration());
  }

}
