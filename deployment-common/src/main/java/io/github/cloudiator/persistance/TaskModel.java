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
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.cloudiator.matchmaking.domain.Requirement;

/**
 * Created by daniel on 12.12.14.
 */
@Entity
class TaskModel extends Model {

  @Column(unique = true, nullable = false)
  private String name;
  @ManyToOne
  private JobModel jobModel;

  @OneToOne
  private OptimizationModel optimizationModel;

  @OneToMany(mappedBy = "task")
  private List<PortModel> ports;

  @OneToMany(mappedBy = "task")
  private List<TaskInterfaceModel> interfaces;

  @OneToMany(mappedBy = "task")
  private List<RequirementModel> requirements;

  /**
   * Empty constructor for hibernate.
   */
  protected TaskModel() {
  }

  public TaskModel(String name, JobModel job, OptimizationModel optimizationModel) {
    checkNotNull(name, "name is null");
    checkArgument(!name.isEmpty(), "name is empty");
    checkNotNull(job, "job is null");
    this.name = name;
    this.jobModel = job;
    this.optimizationModel = optimizationModel;
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

  public List<PortRequiredModel> getRequiredPorts() {
    return getPorts().stream().filter(portModel -> portModel instanceof PortRequiredModel).map(
        portModel -> (PortRequiredModel) portModel).collect(Collectors.toList());
  }

  public List<PortProvidedModel> getProvidedPorts() {
    return getPorts().stream().filter(portModel -> portModel instanceof PortProvidedModel).map(
        portModel -> (PortProvidedModel) portModel).collect(Collectors.toList());
  }

  public List<TaskInterfaceModel> getInterfaces() {
    return ImmutableList.copyOf(interfaces);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("name", name).add("ports", Arrays.toString(ports.toArray()));
  }

  public OptimizationModel getOptimizationModel() {
    return optimizationModel;
  }

  public List<RequirementModel> getRequirements() {
    return ImmutableList.copyOf(requirements);
  }
}
