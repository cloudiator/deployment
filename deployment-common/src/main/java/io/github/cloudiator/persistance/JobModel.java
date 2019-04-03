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
import javax.persistence.OneToOne;

/**
 * Created by daniel on 12.12.14.
 */
@Entity
class JobModel extends Model {

  @Column(nullable = false, unique = true)
  private String uuid;

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private TenantModel tenant;

  @OneToMany(mappedBy = "jobModel")
  private List<TaskModel> tasks;

  @OneToMany(mappedBy = "jobModel")
  private List<CommunicationModel> communications;

  @OneToMany(mappedBy = "job")
  private List<RequirementModel> requirements;

  @OneToOne
  private OptimizationModel optimizationModel;

  /**
   * Empty constructor for hibernate.
   */
  protected JobModel() {
  }

  public JobModel(String uuid, String name, TenantModel tenant) {
    checkNotNull(uuid);
    checkNotNull(name);
    checkArgument(!name.isEmpty());
    checkNotNull(tenant);
    this.uuid = uuid;
    this.name = name;
    this.tenant = tenant;
  }

  public String getName() {
    return name;
  }

  public List<TaskModel> getTasks() {
    return ImmutableList.copyOf(tasks);
  }

  public List<CommunicationModel> getCommunications() {
    return ImmutableList.copyOf(communications);
  }

  public TenantModel getTenant() {
    return tenant;
  }

  public OptimizationModel getOptimizationModel() {
    return optimizationModel;
  }

  public List<RequirementModel> getRequirements() {
    return ImmutableList.copyOf(requirements);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("uuid", uuid).add("name", name)
        .add("tasks", Arrays.toString(tasks.toArray()))
        .add("communications", Arrays.toString(communications.toArray()));
  }

  public String getUuid() {
    return uuid;
  }
}
