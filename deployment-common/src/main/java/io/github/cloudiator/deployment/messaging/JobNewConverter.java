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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.JobNew;
import io.github.cloudiator.deployment.domain.JobNewBuilder;
import org.cloudiator.matchmaking.converters.OptimizationConverter;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.messages.entities.JobEntities;

public class JobNewConverter implements OneWayConverter<JobEntities.JobNew, JobNew> {

  private final CommunicationConverter communicationConverter = new CommunicationConverter();
  private final TaskConverter taskConverter = new TaskConverter();

  public static final JobNewConverter INSTANCE = new JobNewConverter();
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final OptimizationConverter OPTIMIZATION_CONVERTER = OptimizationConverter.INSTANCE;

  private JobNewConverter() {
  }


  @Override
  public JobNew apply(JobEntities.JobNew jobNew) {

    JobNewBuilder jobBuilder = JobNewBuilder.newBuilder().name(jobNew.getName());

    jobNew.getCommunicationsList().forEach(
        communication -> jobBuilder.addCommunication(communicationConverter.apply(communication)));

    jobNew.getTasksList().forEach(task -> jobBuilder.addTask(taskConverter.apply(task)));

    if (jobNew.hasOptimization()) {
      jobBuilder.optimization(OPTIMIZATION_CONVERTER.apply(jobNew.getOptimization()));
    }
    jobNew.getRequirementsList().stream().map(REQUIREMENT_CONVERTER)
        .forEach(jobBuilder::addRequirement);

    return jobBuilder.build();
  }
}
