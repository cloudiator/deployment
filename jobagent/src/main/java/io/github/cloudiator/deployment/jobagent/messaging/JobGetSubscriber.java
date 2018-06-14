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
import io.github.cloudiator.persistance.JobDomainRepository;
import org.cloudiator.messages.Job.JobQueryRequest;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.services.JobService;

public class JobGetSubscriber implements Runnable {

  private final JobService jobService;
  private final JobDomainRepository jobDomainRepository;

  @Inject
  public JobGetSubscriber(JobService jobService,
      JobDomainRepository jobDomainRepository) {
    this.jobService = jobService;
    this.jobDomainRepository = jobDomainRepository;
  }

  @Override
  public void run() {
    jobService.subscribeToJobQueryRequest(new MessageCallback<JobQueryRequest>() {
      @Override
      public void accept(String id, JobQueryRequest content) {

        final String userId = content.getUserId();







      }
    });
  }
}
