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

import com.google.common.collect.ImmutableList;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
class ScheduleModel extends Model {

  @Column(nullable = false)
  private String domainId;

  @ManyToOne(optional = false)
  private TenantModel tenant;

  @Column(nullable = false)
  private String jobId;

  @Enumerated(EnumType.STRING)
  private Schedule.Instantiation instantiation;

  @Enumerated(EnumType.STRING)
  private ScheduleState scheduleState;

  @OneToMany(mappedBy = "schedule")
  private List<ProcessModel> proccesses;

  protected ScheduleModel() {
  }

  ScheduleModel(String domainId, TenantModel tenant, String jobId,
      Instantiation instantiation, ScheduleState scheduleState) {

    checkNotNull(domainId, "domainId is null");
    checkArgument(!domainId.isEmpty(), "domainId is empty");

    checkNotNull(tenant, "tenant is null");

    checkNotNull(jobId, "jobId is null");
    checkArgument(!jobId.isEmpty(), "jobId is empty");

    checkNotNull(instantiation, "instantiation is null");

    checkNotNull(scheduleState, "scheduleState is null");

    this.domainId = domainId;
    this.tenant = tenant;
    this.jobId = jobId;
    this.instantiation = instantiation;
    this.scheduleState = scheduleState;
  }

  public String domainId() {
    return domainId;
  }

  public TenantModel tenant() {
    return tenant;
  }

  public String jobId() {
    return jobId;
  }

  public Instantiation instantiation() {
    return instantiation;
  }

  public List<ProcessModel> processes() {
    return ImmutableList.copyOf(proccesses);
  }

  public ScheduleState state() {
    return scheduleState;
  }
}
