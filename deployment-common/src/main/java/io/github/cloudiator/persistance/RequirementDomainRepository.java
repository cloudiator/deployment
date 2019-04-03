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
import org.cloudiator.matchmaking.domain.AttributeRequirement;
import org.cloudiator.matchmaking.domain.IdRequirement;
import org.cloudiator.matchmaking.domain.OclRequirement;
import org.cloudiator.matchmaking.domain.Requirement;

public class RequirementDomainRepository {

  private final RequirementModelRepository requirementModelRepository;

  @Inject
  public RequirementDomainRepository(
      RequirementModelRepository requirementModelRepository) {
    this.requirementModelRepository = requirementModelRepository;
  }

  RequirementModel saveAndGet(Requirement domain, TaskModel taskModel, JobModel jobModel) {
    final RequirementModel requirementModel = create(domain, taskModel, jobModel);
    requirementModelRepository.save(requirementModel);
    return requirementModel;
  }

  private RequirementModel create(Requirement domain, TaskModel taskModel, JobModel jobModel) {
    if (domain instanceof AttributeRequirement) {
      return createAttributeRequirementModel((AttributeRequirement) domain, taskModel, jobModel);
    } else if (domain instanceof IdRequirement) {
      return createIdentifierRequirementModel((IdRequirement) domain, taskModel, jobModel);
    } else if (domain instanceof OclRequirement) {
      return createOCLRequirementModel((OclRequirement) domain, taskModel, jobModel);
    } else {
      throw new AssertionError("Unknown requirement type " + domain.getClass().getName());
    }
  }

  private AttributeRequirementModel createAttributeRequirementModel(
      AttributeRequirement attributeRequirement, TaskModel taskModel, JobModel jobModel) {
    return new AttributeRequirementModel(taskModel, jobModel, attributeRequirement.requirementClass(),
        attributeRequirement.requirementAttribute(), attributeRequirement.requirementOperator(),
        attributeRequirement.value());
  }

  private IdentifierRequirementModel createIdentifierRequirementModel(IdRequirement domain,
      TaskModel taskModel, JobModel jobModel) {
    return new IdentifierRequirementModel(taskModel, jobModel, domain.hardwareId(), domain.locationId(),
        domain.imageId());
  }

  private OCLRequirementModel createOCLRequirementModel(OclRequirement domain,
      TaskModel taskModel, JobModel jobModel) {
    return new OCLRequirementModel(taskModel, jobModel, domain.constraint());
  }


}
