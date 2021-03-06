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

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Created by daniel on 03.08.15.
 */
@Entity
class PortRequiredModel extends PortModel {

  private static boolean DEFAULT_MANDATORY = false;

  private @OneToOne(mappedBy = "requiredPort")
  CommunicationModel requiredCommunication;

  private Boolean isMandatory;

  /**
   * Default constructor for hibernate.
   */
  protected PortRequiredModel() {
  }

  public PortRequiredModel(String name, TaskModel taskModel,
      @Nullable Boolean isMandatory) {
    super(name, taskModel);

    if (isMandatory == null) {
      this.isMandatory = DEFAULT_MANDATORY;
    } else {
      this.isMandatory = isMandatory;
    }
  }

  public CommunicationModel getRequiredCommunication() {
    return requiredCommunication;
  }

  public Boolean getMandatory() {
    if (isMandatory == null) {
      return DEFAULT_MANDATORY;
    }
    return isMandatory;
  }
}
