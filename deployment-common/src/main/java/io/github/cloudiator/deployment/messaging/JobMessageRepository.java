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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.deployment.domain.Job;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Job.JobQueryRequest;
import org.cloudiator.messages.Job.JobQueryResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.JobService;

public class JobMessageRepository implements MessageRepository<Job> {

  private final JobService jobService;
  private static final JobConverter JOB_CONVERTER = new JobConverter();

  @Inject
  public JobMessageRepository(JobService jobService) {
    this.jobService = jobService;
  }

  @Nullable
  @Override
  public Job getById(String userId, String id) {

    final JobQueryRequest jobQueryRequest = JobQueryRequest.newBuilder().setUserId(userId)
        .setJobId(id).build();

    try {
      final JobQueryResponse jobs = jobService.getJobs(jobQueryRequest);

      return jobs.getJobsList().stream().map(JOB_CONVERTER).collect(StreamUtil.getOnly())
          .orElse(null);

    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve jobs.", e);
    }
  }

  @Override
  public List<Job> getAll(String userId) {

    final JobQueryRequest jobQueryRequest = JobQueryRequest.newBuilder().setUserId(userId).build();
    try {
      return jobService.getJobs(jobQueryRequest).getJobsList().stream().map(JOB_CONVERTER)
          .collect(Collectors
              .toList());
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve jobs.", e);
    }
  }
}
