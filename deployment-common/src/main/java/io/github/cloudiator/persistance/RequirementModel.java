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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class RequirementModel extends Model {

  @ManyToOne
  private TaskModel task;

  @ManyToOne
  private JobModel job;

  protected RequirementModel() {

  }

  RequirementModel(TaskModel task, JobModel job) {

    if ((job != null && task != null) || (job == null && task == null)) {
      throw new IllegalArgumentException("Either job or task need to be set");
    }

    this.task = task;
    this.job = job;
  }

  RequirementModel(TaskModel task) {
    checkNotNull(task, "task is null");
    this.task = task;
    this.job = null;
  }

  RequirementModel(JobModel job) {
    checkNotNull(job, "job is null;");
    this.task = null;
    this.job = job;
  }

}
