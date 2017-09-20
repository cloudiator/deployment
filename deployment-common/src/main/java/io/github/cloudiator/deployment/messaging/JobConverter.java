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
import org.cloudiator.messages.entities.JobEntities;

public class JobConverter implements TwoWayConverter<JobEntities.Job, Job> {

  private final CommunicationConverter communicationConverter = new CommunicationConverter();
  private final TaskConverter taskConverter = new TaskConverter();

  @Override
  public JobEntities.Job applyBack(Job job) {

    return JobEntities.Job.newBuilder().setName(job.name()).addAllCommunications(
        job.communications().stream()
            .map(communicationConverter::applyBack).collect(
            Collectors.toList())).addAllTasks(
        job.tasks().stream().map(taskConverter::applyBack).collect(Collectors.toList()))
        .build();
  }

  @Override
  public Job apply(JobEntities.Job job) {

    JobBuilder jobBuilder = JobBuilder.newBuilder().name(job.getName());

    job.getCommunicationsList().forEach(
        communication -> jobBuilder.addCommunication(communicationConverter.apply(communication)));

    job.getTasksList().forEach(task -> jobBuilder.addTask(taskConverter.apply(task)));

    return jobBuilder.build();
  }
}
