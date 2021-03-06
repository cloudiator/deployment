/*
 * Copyright 2017 University of Ulm
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

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.converters.OptimizationConverter;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.messages.entities.JobEntities;
import org.cloudiator.messages.entities.JobEntities.Job.Builder;

public class JobConverter implements TwoWayConverter<JobEntities.Job, Job> {

  public static final JobConverter INSTANCE = new JobConverter();

  private JobConverter() {

  }

  private final CommunicationConverter communicationConverter = new CommunicationConverter();
  private final TaskConverter taskConverter = new TaskConverter();
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final OptimizationConverter OPTIMIZATION_CONVERTER = OptimizationConverter.INSTANCE;

  @Override
  public JobEntities.Job applyBack(Job job) {

    final Builder builder = JobEntities.Job.newBuilder().setId(job.id()).setUserId(job.userId())
        .setName(job.name())
        .addAllCommunications(
            job.communications().stream()
                .map(communicationConverter::applyBack).collect(
                Collectors.toList())).addAllTasks(
            job.tasks().stream().map(taskConverter::applyBack).collect(Collectors.toList()));

    job.requirements().stream().map(REQUIREMENT_CONVERTER::applyBack)
        .forEach(builder::addRequirements);

    if (job.optimization().isPresent()) {
      builder.setOptimization(OPTIMIZATION_CONVERTER.applyBack(job.optimization().get()));
    }

    return builder.build();

  }

  @Override
  public Job apply(JobEntities.Job job) {

    JobBuilder jobBuilder = JobBuilder.newBuilder().name(job.getName()).id(job.getId())
        .userId(job.getUserId());

    job.getCommunicationsList().forEach(
        communication -> jobBuilder.addCommunication(communicationConverter.apply(communication)));

    job.getTasksList().forEach(task -> jobBuilder.addTask(taskConverter.apply(task)));

    if (job.hasOptimization()) {
      jobBuilder.optimization(OPTIMIZATION_CONVERTER.apply(job.getOptimization()));
    }
    job.getRequirementsList().stream().map(REQUIREMENT_CONVERTER)
        .forEach(jobBuilder::addRequirement);

    return jobBuilder.build();
  }
}
