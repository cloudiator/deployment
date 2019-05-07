/*
 * Copyright 2014-2019 University of Ulm
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

import com.google.common.base.MoreObjects.ToStringHelper;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;


/**
 * Created by daniel on 12.12.14.
 */
@Entity
class ProcessClusterModel extends ProcessModel {

  @Column(nullable = false)
  @ElementCollection
  private List<String> nodes;

  /**
   * Empty constructor for hibernate
   */
  protected ProcessClusterModel() {
  }

  public ProcessClusterModel(String domainId, String originId, ScheduleModel schedule, String task,
      String taskInterface,
      ProcessState state, Type type,
      List<String> nodes, @Nullable String diagnostic, @Nullable String reason,
      @Nullable String endpoint,
      IpGroupModel ipGroupModel) {
    super(domainId, originId, schedule, task, taskInterface, state, type,
        diagnostic, reason, endpoint, ipGroupModel);

    checkNotNull(nodes, "nodes is null");

    this.nodes = nodes;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("nodes", nodes);
  }

  public List<String> getNodes() {
    return nodes;
  }
}
