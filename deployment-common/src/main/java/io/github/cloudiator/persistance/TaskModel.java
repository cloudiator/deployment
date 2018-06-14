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
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 12.12.14.
 */
@Entity
class TaskModel extends Model {

  @Column(unique = true, nullable = false)
  private String name;
  @ManyToOne
  private JobModel jobModel;
  @OneToMany(mappedBy = "task")
  private List<PortModel> ports;
  @OneToMany(mappedBy = "taskModel")
  private List<TaskInterfaceModel> interfaces;

  /**
   * Empty constructor for hibernate.
   */
  protected TaskModel() {
  }

  public TaskModel(String name, JobModel job) {
    checkNotNull(name);
    checkArgument(!name.isEmpty());
    checkNotNull(job);
    this.name = name;
    this.jobModel = job;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JobModel getJobModel() {
    return jobModel;
  }

  public void setJobModel(JobModel jobModel) {
    this.jobModel = jobModel;
  }

  public List<PortModel> getPorts() {
    return ImmutableList.copyOf(ports);
  }

  public List<TaskInterfaceModel> getInterfaces() {
    return ImmutableList.copyOf(interfaces);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("name", name).add("ports", Arrays.toString(ports.toArray()));
  }
}
