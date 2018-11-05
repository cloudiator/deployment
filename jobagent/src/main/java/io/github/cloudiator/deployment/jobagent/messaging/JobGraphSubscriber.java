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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.graph.Graphs;
import io.github.cloudiator.deployment.graph.JobGraph;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Job.JobGraphRequest;
import org.cloudiator.messages.Job.JobGraphResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobGraphSubscriber implements Runnable {


  private static final Logger LOGGER = LoggerFactory.getLogger(JobGraphSubscriber.class);

  private final JobService jobService;
  private final MessageInterface messageInterface;
  private final JobMessageRepository jobMessageRepository;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Inject
  public JobGraphSubscriber(JobService jobService,
      MessageInterface messageInterface,
      JobMessageRepository jobMessageRepository) {
    this.jobService = jobService;
    this.messageInterface = messageInterface;
    this.jobMessageRepository = jobMessageRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(JobGraphRequest.class, JobGraphRequest.parser(),
        (id, content) -> {

          final String userId = content.getUserId();
          final String jobId = content.getJobId();

          try {

            final Job byId = jobMessageRepository.getById(userId, jobId);

            if (byId == null) {
              messageInterface.reply(JobGraphResponse.class, id, Error.newBuilder().setCode(404)
                  .setMessage(String.format("Job with id %s does not exist.", jobId)).build());
              return;
            }

            final JobGraph jobGraph = Graphs.jobGraph(byId);

            messageInterface.reply(id, JobGraphResponse.newBuilder()
                .setJson(OBJECT_MAPPER.writeValueAsString(jobGraph.toJson())).build());

          } catch (Exception e) {
            LOGGER.error("Unexpected exception while generating graph: " + e.getMessage(), e);
            messageInterface.reply(JobGraphResponse.class, id, Error.newBuilder().setCode(500)
                .setMessage(String
                    .format("Unexpected exception while generating graph for request %s.", content))
                .build());
          }

        });
  }
}
