/*
 * Copyright 2014-2018 University of Ulm
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

import org.cloudiator.messages.Process.CreateScheduleRequest;

public class ScheduleRequest {

  private final String id;
  private final CreateScheduleRequest createScheduleRequest;

  public static ScheduleRequest of(String id, CreateScheduleRequest createScheduleRequest) {
    return new ScheduleRequest(id, createScheduleRequest);
  }

  private ScheduleRequest(String id,
      CreateScheduleRequest createScheduleRequest) {
    this.id = id;
    this.createScheduleRequest = createScheduleRequest;
  }

  public String getId() {
    return id;
  }

  public CreateScheduleRequest getCreateScheduleRequest() {
    return createScheduleRequest;
  }
}
