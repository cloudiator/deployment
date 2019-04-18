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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.messaging.ScheduleConverter;
import io.github.cloudiator.deployment.scheduler.failure.ScheduleEventReportingInterface;
import org.cloudiator.messages.Process.ScheduleEvent;
import org.cloudiator.messaging.MessageInterface;

public class ScheduleEventSubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final ScheduleEventReportingInterface scheduleEventReportingInterface;

  @Inject
  public ScheduleEventSubscriber(MessageInterface messageInterface,
      ScheduleEventReportingInterface scheduleEventReportingInterface) {
    this.messageInterface = messageInterface;
    this.scheduleEventReportingInterface = scheduleEventReportingInterface;
  }

  @Override
  public void run() {
    messageInterface.subscribe(ScheduleEvent.class, ScheduleEvent.parser(),
        (id, content) -> scheduleEventReportingInterface
            .announceSchedule(ScheduleConverter.INSTANCE.apply(content.getSchedule()),
                ScheduleConverter.SCHEDULE_STATE_CONVERTER.apply(content.getFrom())));
  }
}
