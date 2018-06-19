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

import com.google.inject.Inject;
import org.cloudiator.matchmaking.domain.AttributeOptimization;
import org.cloudiator.matchmaking.domain.OCLOptimization;
import org.cloudiator.matchmaking.domain.Optimization;

public class OptimizationDomainRepository {

  private final OptimizationModelRepository optimizationModelRepository;

  @Inject
  public OptimizationDomainRepository(
      OptimizationModelRepository optimizationModelRepository) {
    this.optimizationModelRepository = optimizationModelRepository;
  }

  OptimizationModel saveAndGet(Optimization domain) {
    final OptimizationModel optimizationModel = create(domain);
    optimizationModelRepository.save(optimizationModel);
    return optimizationModel;
  }

  private OptimizationModel create(Optimization domain) {
    if (domain instanceof AttributeOptimization) {
      return createAttributeOptimization((AttributeOptimization) domain);
    } else if (domain instanceof OCLOptimization) {
      return createOCLOptimization((OCLOptimization) domain);
    } else {
      throw new AssertionError("Unknown optimization type " + domain.getClass().getName());
    }
  }

  private OptimizationModel createAttributeOptimization(AttributeOptimization attributeOptimization) {
    return new AttributeOptimizationModel(attributeOptimization.objective(),
        attributeOptimization.objectiveClass(), attributeOptimization.objectiveAttribute(),
        attributeOptimization.aggregation());
  }

  private OptimizationModel createOCLOptimization(OCLOptimization oclOptimization) {
    return new OCLOptimizationModel(oclOptimization.objective(), oclOptimization.expression());
  }

}
