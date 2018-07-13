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

package io.github.cloudiator.deployment.domain;

import java.util.UUID;

public class ScheduleImpl implements Schedule {

  private final String id;
  private final Job job;

  public ScheduleImpl(String id, Job job) {
    this.id = id;
    this.job = job;
  }

  public static Schedule of(Job job) {
    return new ScheduleImpl(UUID.randomUUID().toString(), job);
  }


  @Override
  public Job job() {
    return job;
  }

  @Override
  public String id() {
    return id;
  }
}
