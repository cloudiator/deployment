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

import javax.persistence.Column;
import javax.persistence.Entity;
import org.cloudiator.matchmaking.domain.RequirementOperator;

@Entity
class AttributeRequirementModel extends RequirementModel {

  @Column(nullable = false)
  private String requirementClass;

  @Column(nullable = false)
  private String requirementAttribute;

  @Column(nullable = false)
  private RequirementOperator requirementOperator;

  @Column(nullable = false)
  private String value;

  protected AttributeRequirementModel() {

  }

  AttributeRequirementModel(TaskModel task, JobModel job, String requirementClass,
      String requirementAttribute, RequirementOperator requirementOperator, String value) {
    super(task, job);

    checkNotNull(requirementClass, "requirementClass is null");
    checkArgument(!requirementClass.isEmpty(), "requirementClass is empty");
    this.requirementClass = requirementClass;

    checkNotNull(requirementAttribute, "requirementAttribute is null");
    checkArgument(!requirementAttribute.isEmpty(), "requirementAttribute is emtpy");
    this.requirementAttribute = requirementAttribute;

    checkNotNull(requirementOperator, "requirementOperator is null");
    this.requirementOperator = requirementOperator;

    checkNotNull(value, "value is null");
    checkArgument(!value.isEmpty(), "value is empty");
    this.value = value;

  }

  public String getRequirementClass() {
    return requirementClass;
  }

  public String getRequirementAttribute() {
    return requirementAttribute;
  }

  public RequirementOperator getRequirementOperator() {
    return requirementOperator;
  }

  public String getValue() {
    return value;
  }
}
