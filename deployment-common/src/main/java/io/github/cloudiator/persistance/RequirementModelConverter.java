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

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.AttributeRequirementBuilder;
import org.cloudiator.matchmaking.domain.IdRequirement;
import org.cloudiator.matchmaking.domain.OclRequirement;
import org.cloudiator.matchmaking.domain.Requirement;

class RequirementModelConverter implements OneWayConverter<RequirementModel, Requirement> {

  @Nullable
  @Override
  public Requirement apply(@Nullable RequirementModel requirementModel) {
    if (requirementModel == null) {
      return null;
    }

    if (requirementModel instanceof OCLRequirementModel) {
      return OclRequirement.of(((OCLRequirementModel) requirementModel).getOclConstraint());
    } else if (requirementModel instanceof AttributeRequirementModel) {
      return AttributeRequirementBuilder.newBuilder().requirementAttribute(
          ((AttributeRequirementModel) requirementModel).getRequirementAttribute())
          .requirementClass(((AttributeRequirementModel) requirementModel).getRequirementClass())
          .requirementOperator(
              ((AttributeRequirementModel) requirementModel).getRequirementOperator())
          .value(((AttributeRequirementModel) requirementModel).getValue()).build();
    } else if (requirementModel instanceof IdentifierRequirementModel) {
      return IdRequirement.of(((IdentifierRequirementModel) requirementModel).getHardwareId(),
          ((IdentifierRequirementModel) requirementModel).getLocationId(),
          ((IdentifierRequirementModel) requirementModel).getImageId());
    } else {
      throw new AssertionError(
          "Unknown requirement model type " + requirementModel.getClass().getName());
    }

  }
}
