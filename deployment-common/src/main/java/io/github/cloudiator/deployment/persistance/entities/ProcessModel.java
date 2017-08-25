/*
 * Copyright (c) 2014-2017 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.deployment.persistance.entities;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.persistance.entities.Model;
import io.github.cloudiator.deployment.domain.Process;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * Created by daniel on 12.12.14.
 */
@Entity
public class ProcessModel extends Model {

  @ManyToOne(optional = false)
  private TaskModel task;
  @Enumerated
  private Process.State state;

  /**
   * Empty constructor for hibernate
   */
  protected ProcessModel() {
  }

  public ProcessModel(TaskModel task, Process.State state) {
    checkNotNull(task, "task is null");
    this.task = task;
    checkNotNull(state, "state is null");
    this.state = state;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("task", task).add("state", state);
  }
}
