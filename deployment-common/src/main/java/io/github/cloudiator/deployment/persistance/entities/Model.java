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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Superclass for all model classes.
 * <p>
 * Defines the auto generated id for
 * each model class.
 */
@MappedSuperclass
public abstract class Model implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  private Long id;

  /**
   * Empty constructor for hibernate.
   */
  protected Model() {
  }

  /**
   * Getter for the id.
   *
   * @return the identifies for this model object.
   */
  public Long getId() {
    return id;
  }

  /**
   * Setter for the id.
   *
   * @param id the identified for this model object
   */
  public void setId(Long id) {
    this.id = id;
  }

  protected ToStringHelper stringHelper() {
    return MoreObjects.toStringHelper(this).add("id", id);
  }

  @Override
  public String toString() {
    return stringHelper().toString();
  }
}
