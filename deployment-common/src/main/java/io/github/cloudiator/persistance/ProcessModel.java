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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.State;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * Created by daniel on 12.12.14.
 */
@Entity
class ProcessModel extends Model {

  @Column(unique = true, nullable = false)
  private String domainId;

  @ManyToOne(optional = false)
  private ScheduleModel schedule;

  @Column(unique = true, nullable = false)
  private String task;

  @Column(nullable = false)
  private String node;

  @Enumerated(EnumType.STRING)
  private CloudiatorProcess.Type type;

  @Enumerated(EnumType.STRING)
  private CloudiatorProcess.State state;

  /**
   * Empty constructor for hibernate
   */
  protected ProcessModel() {
  }

  public ProcessModel(String domainId, ScheduleModel schedule, String task, String node,
      CloudiatorProcess.State state, CloudiatorProcess.Type type) {

    checkNotNull(domainId, "domainId is null");
    checkArgument(!domainId.isEmpty(), "domainId is empty");

    checkNotNull(schedule, "schedule is null");

    checkNotNull(task, "task is null");
    checkArgument(!task.isEmpty(), "task is empty");

    checkNotNull(node, "node is null");
    checkArgument(!node.isEmpty(), "node is empty");

    checkNotNull(type, "type is null");

    this.domainId = domainId;
    this.schedule = schedule;
    this.task = task;
    this.state = state;
    this.node = node;
    this.type = type;

  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("domainId", domainId).add("schedule", schedule)
        .add("task", task).add("state", state);
  }

  public String getNode() {
    return node;
  }

  public String getDomainId() {
    return domainId;
  }

  public ScheduleModel getSchedule() {
    return schedule;
  }

  public String getTask() {
    return task;
  }

  public State getState() {
    return state;
  }

  public Type getType() {
    return type;
  }
}
