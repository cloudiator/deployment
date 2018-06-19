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

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.AttributeOptimizationBuilder;
import org.cloudiator.matchmaking.domain.OCLOptimization;
import org.cloudiator.matchmaking.domain.Optimization;

class OptimizationModelConverter implements
    OneWayConverter<OptimizationModel, Optimization> {

  @Nullable
  @Override
  public Optimization apply(@Nullable OptimizationModel optimizationModel) {
    if (optimizationModel == null) {
      return null;
    }

    if (optimizationModel instanceof AttributeOptimizationModel) {
      return AttributeOptimizationBuilder.newBuilder()
          .objectiveClass(((AttributeOptimizationModel) optimizationModel).getObjectiveClass())
          .objective(optimizationModel.getObjective()).objectiveAttribute(
              ((AttributeOptimizationModel) optimizationModel).getObjectiveAttribute())
          .aggregation(((AttributeOptimizationModel) optimizationModel).getAggregation()).build();
    } else if (optimizationModel instanceof OCLOptimizationModel) {
      return OCLOptimization.of(optimizationModel.getObjective(),
          ((OCLOptimizationModel) optimizationModel).getExpression());
    } else {
      throw new AssertionError(
          "Unknown optimization model type" + optimizationModel.getClass().getName());
    }
  }
}
