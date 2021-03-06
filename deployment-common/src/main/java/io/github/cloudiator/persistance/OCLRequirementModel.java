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

@Entity
public class OCLRequirementModel extends RequirementModel {

  @Column(nullable = false)
  private String oclConstraint;

  protected OCLRequirementModel() {

  }

  OCLRequirementModel(TaskModel task, JobModel job, String oclConstraint) {
    super(task, job);
    checkNotNull(oclConstraint, "oclConstraint is null");
    checkArgument(!oclConstraint.isEmpty(), "oclConstraint is empty");
    this.oclConstraint = oclConstraint;
  }

  public String getOclConstraint() {
    return oclConstraint;
  }
}
