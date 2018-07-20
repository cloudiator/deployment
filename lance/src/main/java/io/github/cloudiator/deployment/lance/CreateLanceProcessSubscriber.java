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

package io.github.cloudiator.deployment.lance;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.LanceProcess;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLanceProcessSubscriber implements Runnable {

  private final ProcessService processService;
  private final NodeToNodeMessageConverter nodeMessageToNodeConverter = new NodeToNodeMessageConverter();
  private final JobConverter jobConverter = new JobConverter();
  private final CreateLanceProcessStrategy createLanceProcessStrategy;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLanceProcessSubscriber.class);

  @Inject
  public CreateLanceProcessSubscriber(
      ProcessService processService,
      LanceInstallationStrategy lanceInstallationStrategy,
      CreateLanceProcessStrategy createLanceProcessStrategy) {
    this.processService = processService;
    this.createLanceProcessStrategy = createLanceProcessStrategy;
  }

  @Override
  public void run() {
    processService.subscribeCreateLanceProcessRequest(
        (id, content) -> {

          try {

            //todo: reply

            final String userId = content.getUserId();
            final Job job = jobConverter.apply(content.getLance().getJob());
            final String task = content.getLance().getTask();
            final Node node = nodeMessageToNodeConverter.applyBack(content.getLance().getNode());
            final Schedule schedule = new ScheduleImpl(content.getLance().getSchedule().getId(),
                job);

            final LanceProcess lanceProcess = createLanceProcessStrategy
                .execute(userId, schedule, job.getTask(task).orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Job %s does not contain task %s", job, task))), node);
          } catch (Exception e) {
            LOGGER.error(String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id), e);
          }


        });
  }
}
