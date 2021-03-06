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

import com.google.common.base.MoreObjects.ToStringHelper;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * Created by daniel on 03.08.15.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class PortModel extends Model {

  @Column(updatable = false, nullable = false)
  private String name;
  @ManyToOne(optional = false)
  private TaskModel task;

  /**
   * Default constructor for hibernate.
   */
  protected PortModel() {
  }

  public PortModel(String name, TaskModel task) {

    checkNotNull(name);
    checkArgument(!name.isEmpty());
    checkNotNull(task);

    this.name = name;
    this.task = task;
  }

  public String getName() {
    return name;
  }

  public TaskModel getTask() {
    return task;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("name", name).add("task", task);
  }
}
