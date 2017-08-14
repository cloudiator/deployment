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
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 03.08.15.
 */
@Entity
public class PortProvidedModel extends PortModel {

  @Column(nullable = false)
  private Integer port;
  @OneToMany(mappedBy = "providedPort")
  List<CommunicationModel> providedCommunications;

  /**
   * Empty constructor for hibernate.
   */
  protected PortProvidedModel() {
  }

  public PortProvidedModel(String name, TaskModel task, Integer port) {
    super(name, task);
    checkNotNull(port, "port is null");
  }

  public int getPort() {
    return port;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("port", port)
        .add("providedCommunications", Arrays.toString(providedCommunications.toArray()));
  }
}
