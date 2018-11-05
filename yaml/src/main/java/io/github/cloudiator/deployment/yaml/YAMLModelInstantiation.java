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

package io.github.cloudiator.deployment.yaml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.yaml.model.YAMLModel;
import io.github.cloudiator.rest.converter.JobNewConverter;
import org.cloudiator.messages.Job.CreateJobRequest;
import org.cloudiator.messages.Job.JobCreatedResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.JobService;

public class YAMLModelInstantiation {

  public static class YAMLModelInstantiationFactory {

    private final JobService jobService;

    @Inject
    public YAMLModelInstantiationFactory(JobService jobService) {
      this.jobService = jobService;
    }

    public YAMLModelInstantiation create(YAMLModel yamlModel, String userId) {
      checkNotNull(yamlModel, "yamlModel is null");
      checkNotNull(userId, "userId is null");
      checkArgument(!userId.isEmpty(), "userId is empty");

      return new YAMLModelInstantiation(yamlModel, userId, jobService);
    }

  }

  private final JobService jobService;
  private final String userId;
  private final YAMLModel yamlModel;

  private YAMLModelInstantiation(YAMLModel yamlModel, String userId, JobService jobService) {
    this.yamlModel = yamlModel;
    this.userId = userId;
    this.jobService = jobService;
  }

  public Job instantiate() {

    //generate the job
    return createJob();

  }

  private Job createJob() {
    checkArgument(yamlModel.getJob() != null, "job entity of yaml model is null");

    try {
      final JobCreatedResponse job = jobService
          .createJob(CreateJobRequest.newBuilder().setUserId(userId)
              .setJob(new JobNewConverter().apply(yamlModel.getJob())).build());

      return JobConverter.INSTANCE.apply(job.getJob());

    } catch (ResponseException e) {
      throw new IllegalStateException(
          "could not initialize yaml model because of " + e.getMessage(),
          e);
    }

  }


}
