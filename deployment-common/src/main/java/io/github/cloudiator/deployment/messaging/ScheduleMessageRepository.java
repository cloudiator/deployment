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
import io.github.cloudiator.deployment.domain.Schedule;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Process.ScheduleQueryRequest;
import org.cloudiator.messages.Process.ScheduleQueryResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;

public class ScheduleMessageRepository implements MessageRepository<Schedule> {

  private final ProcessService processService;
  private static final ScheduleConverter SCHEDULE_CONVERTER = ScheduleConverter.INSTANCE;

  @Inject
  public ScheduleMessageRepository(ProcessService processService) {
    this.processService = processService;
  }

  @Nullable
  @Override
  public Schedule getById(String userId, String id) {

    try {
      final ScheduleQueryResponse scheduleQueryResponse = processService.querySchedules(
          ScheduleQueryRequest.newBuilder().setUserId(userId).setScheduleId(id)
              .build());

      return scheduleQueryResponse.getSchedulesList().stream().map(SCHEDULE_CONVERTER)
          .collect(StreamUtil.getOnly())
          .orElse(null);


    } catch (ResponseException e) {
      throw new IllegalStateException("Could not query schedules.");
    }
  }

  @Override
  public List<Schedule> getAll(String userId) {
    try {
      final ScheduleQueryResponse scheduleQueryResponse = processService.querySchedules(
          ScheduleQueryRequest.newBuilder().setUserId(userId)
              .build());

      return scheduleQueryResponse.getSchedulesList().stream().map(SCHEDULE_CONVERTER)
          .collect(Collectors.toList());


    } catch (ResponseException e) {
      throw new IllegalStateException("Could not query schedules.");
    }
  }
}
