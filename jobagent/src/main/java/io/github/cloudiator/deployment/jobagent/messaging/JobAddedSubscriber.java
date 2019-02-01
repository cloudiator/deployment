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

package io.github.cloudiator.deployment.jobagent.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import io.github.cloudiator.deployment.domain.JobNew;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.JobNewConverter;
import io.github.cloudiator.deployment.validation.ModelValidationException;
import io.github.cloudiator.deployment.validation.ModelValidationService;
import io.github.cloudiator.persistance.JobDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Job.JobCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobAddedSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobAddedSubscriber.class);

  private final JobService jobService;
  private final MessageInterface messageInterface;
  private final JobDomainRepository jobDomainRepository;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final JobNewConverter JOB_NEW_CONVERTER = JobNewConverter.INSTANCE;
  private final ModelValidationService modelValidationService;

  @Inject
  public JobAddedSubscriber(JobService jobService,
      MessageInterface messageInterface,
      JobDomainRepository jobDomainRepository,
      ModelValidationService modelValidationService) {
    this.jobService = jobService;
    this.messageInterface = messageInterface;
    this.jobDomainRepository = jobDomainRepository;
    this.modelValidationService = modelValidationService;
  }


  @Transactional
  void persistJob(Job job) {
    jobDomainRepository
        .save(job);
  }


  @Override
  public void run() {
    jobService.subscribeToCreateJobRequest((id, createJobRequest) -> {

      try {

        JobNew jobNew = JOB_NEW_CONVERTER.apply(createJobRequest.getJob());

        Job job = JobBuilder.newBuilder().generateId().name(jobNew.name())
            .userId(createJobRequest.getUserId())
            .addCommunications(jobNew.communications())
            .addTasks(jobNew.tasks()).build();

        try {
          modelValidationService.validate(job);
        } catch (ModelValidationException e) {
          LOGGER.error(e.getMessage(), e);
          messageInterface.reply(JobCreatedResponse.class, id, Error.newBuilder().setCode(400)
              .setMessage("Job model contains errors: " + e.getMessage()).build());
          return;
        }

        persistJob(job);

        final JobCreatedResponse jobCreatedResponse = JobCreatedResponse.newBuilder()
            .setJob(JOB_CONVERTER.applyBack(job)).build();

        messageInterface.reply(id, jobCreatedResponse);

      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        messageInterface.reply(JobCreatedResponse.class, id,
            Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
      }
    });
  }
}
