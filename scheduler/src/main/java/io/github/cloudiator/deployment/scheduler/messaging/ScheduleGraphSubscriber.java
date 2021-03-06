/*
 * Copyright 2014-2019 University of Ulm
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

package io.github.cloudiator.deployment.scheduler.messaging;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.graph.Graphs;
import io.github.cloudiator.deployment.graph.ScheduleGraph;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ScheduleGraphRequest;
import org.cloudiator.messages.Process.ScheduleGraphResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleGraphSubscriber implements Runnable {


  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleGraphSubscriber.class);

  private final MessageInterface messageInterface;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final JobMessageRepository jobMessageRepository;
  private final NodeMessageRepository nodeMessageRepository;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Inject
  public ScheduleGraphSubscriber(MessageInterface messageInterface,
      ScheduleDomainRepository scheduleDomainRepository,
      JobMessageRepository jobMessageRepository,
      NodeMessageRepository nodeMessageRepository) {
    this.messageInterface = messageInterface;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.jobMessageRepository = jobMessageRepository;
    this.nodeMessageRepository = nodeMessageRepository;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule retrieveSchedule(String scheduleId, String userId) {
    return scheduleDomainRepository.findByIdAndUser(scheduleId, userId);
  }


  @Override
  public void run() {
    messageInterface.subscribe(ScheduleGraphRequest.class, ScheduleGraphRequest.parser(),
        (id, content) -> {

          final String userId = content.getUserId();
          final String scheduleId = content.getScheduleId();

          try {

            final Schedule schedule = retrieveSchedule(scheduleId, userId);

            if (schedule == null) {
              messageInterface
                  .reply(ScheduleGraphResponse.class, id, Error.newBuilder().setCode(404)
                      .setMessage(String.format("Schedule with id %s does not exist.", scheduleId))
                      .build());
              return;
            }

            final Job job = jobMessageRepository.getById(userId, schedule.job());

            checkState(job != null,
                String.format("Schedule references job %s but this job does not exist.",
                    schedule.job()));

            final ScheduleGraph scheduleGraph = Graphs.scheduleGraph(schedule, job);

            messageInterface.reply(id, ScheduleGraphResponse.newBuilder()
                .setJson(OBJECT_MAPPER.writeValueAsString(scheduleGraph.toJson())).build());

          } catch (Exception e) {
            LOGGER.error("Unexpected exception while generating graph: " + e.getMessage(), e);
            messageInterface.reply(ScheduleGraphResponse.class, id, Error.newBuilder().setCode(500)
                .setMessage(String
                    .format("Unexpected exception while generating graph for request %s.", content))
                .build());
          }

        });
  }
}
