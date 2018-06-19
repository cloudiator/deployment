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

import javax.persistence.Column;
import javax.persistence.Entity;
import org.cloudiator.matchmaking.domain.AttributeOptimization.Aggregation;
import org.cloudiator.matchmaking.domain.Objective;


@Entity
class AttributeOptimizationModel extends OptimizationModel {

  @Column(nullable = false)
  private String objectiveClass;

  @Column(nullable = false)
  private String objectiveAttribute;

  @Column(nullable = false)
  private Aggregation aggregation;

  protected AttributeOptimizationModel() {

  }

  public AttributeOptimizationModel(Objective objective, String objectiveClass,
      String objectiveAttribute,
      Aggregation aggregation) {
    super(objective);
    checkNotNull(objectiveClass, "objectiveClass is null");
    checkArgument(!objectiveClass.isEmpty(), "objectiveClass is empty");
    this.objectiveClass = objectiveClass;
    checkNotNull(objectiveAttribute, "objectiveAttribute is null");
    checkArgument(!objectiveAttribute.isEmpty(), "objectiveAttribute is empty");
    this.objectiveAttribute = objectiveAttribute;
    checkNotNull(aggregation, "aggregation is null");
    this.aggregation = aggregation;
  }

  public String getObjectiveClass() {
    return objectiveClass;
  }

  public String getObjectiveAttribute() {
    return objectiveAttribute;
  }

  public Aggregation getAggregation() {
    return aggregation;
  }
}
