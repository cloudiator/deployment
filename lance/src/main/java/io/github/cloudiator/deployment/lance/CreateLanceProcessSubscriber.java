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
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.ScheduleImpl;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messages.Process.CreateLanceProcessRequest;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.services.ProcessService;

public class CreateLanceProcessSubscriber implements Runnable {

  private final ProcessService processService;
  private final NodeToNodeMessageConverter nodeMessageToNodeConverter = new NodeToNodeMessageConverter();
  private final JobConverter jobConverter = new JobConverter();
  private final LanceInstallationStrategy lanceInstallationStrategy;
  private final CreateLanceProcessStrategy createLanceProcessStrategy;

  @Inject
  public CreateLanceProcessSubscriber(
      ProcessService processService,
      LanceInstallationStrategy lanceInstallationStrategy,
      CreateLanceProcessStrategy createLanceProcessStrategy) {
    this.processService = processService;
    this.lanceInstallationStrategy = lanceInstallationStrategy;
    this.createLanceProcessStrategy = createLanceProcessStrategy;
  }

  @Override
  public void run() {
    processService.subscribeCreateLanceProcessRequest(
        new MessageCallback<CreateLanceProcessRequest>() {
          @Override
          public void accept(String id, CreateLanceProcessRequest content) {
            final String userId = content.getUserId();
            final Job job = jobConverter.apply(content.getLance().getJob());
            final String task = content.getLance().getTask();
            final Node node = nodeMessageToNodeConverter.applyBack(content.getLance().getNode());
            final Schedule schedule = new ScheduleImpl(content.getLance().getSchedule().getId(),
                job);

            lanceInstallationStrategy.execute(userId, node);

            createLanceProcessStrategy.execute(schedule, job.getTask(task).orElseThrow(
                () -> new IllegalStateException(
                    String.format("Job %s does not contain task %s", job, task))), node);

          }
        });
  }
}
