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

package io.github.cloudiator.deployment.jobagent.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.persistance.JobDomainRepository;
import java.util.List;
import java.util.Optional;
import org.cloudiator.messages.Job.JobQueryRequest;
import org.cloudiator.messages.Job.JobQueryResponse;
import org.cloudiator.messages.Job.JobQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.JobService;

public class JobGetSubscriber implements Runnable {

  private final JobService jobService;
  private final JobDomainRepository jobDomainRepository;
  private static final JobConverter JOB_CONVERTER = new JobConverter();
  private final MessageInterface messageInterface;

  @Inject
  public JobGetSubscriber(JobService jobService,
      JobDomainRepository jobDomainRepository,
      MessageInterface messageInterface) {
    this.jobService = jobService;
    this.jobDomainRepository = jobDomainRepository;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    jobService.subscribeToJobQueryRequest(new MessageCallback<JobQueryRequest>() {
      @Override
      public void accept(String id, JobQueryRequest content) {

        final String userId = content.getUserId();
        final String jobId = content.getJobId();

        if (jobId == null || jobId.isEmpty()) {
          messageInterface.reply(id, retrieveMultiple(userId));
        } else {
          messageInterface.reply(id, retrieveSingle(userId, jobId));
        }
      }
    });
  }

  private JobQueryResponse retrieveSingle(String userId, String jobId) {

    final Optional<Job> optionalJob = retrieveJob(userId, jobId);
    return optionalJob.map(job -> JobQueryResponse.newBuilder()
        .addJobs(JOB_CONVERTER.applyBack(job)).build())
        .orElseGet(() -> JobQueryResponse.newBuilder().build());
  }

  private JobQueryResponse retrieveMultiple(String userId) {
    Builder responseBuilder = JobQueryResponse.newBuilder();
    retrieveStoredJobs(userId).stream().map(JOB_CONVERTER::applyBack).forEach(
        responseBuilder::addJobs);
    return responseBuilder.build();
  }

  @Transactional
  List<Job> retrieveStoredJobs(String userId) {
    return jobDomainRepository.findByUserId(userId);
  }

  @Transactional
  Optional<Job> retrieveJob(String userId, String jobId) {
    return jobDomainRepository.findByUserAndId(userId, jobId);
  }

}
