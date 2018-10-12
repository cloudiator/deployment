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

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ScheduleImpl implements Schedule {

  private final String id;
  private final String job;
  private final Set<CloudiatorProcess> processes;
  private final Instantiation instantiation;

  private ScheduleImpl(String id, String job,
      Instantiation instantiation) {
    this.id = id;
    this.job = job;
    this.instantiation = instantiation;
    this.processes = new HashSet<>();
  }

  public static Schedule create(Job job, Instantiation instantiation) {
    return new ScheduleImpl(UUID.randomUUID().toString(), job.id(), instantiation);
  }

  public static Schedule create(String id, String jobId, Instantiation instantiation) {
    return new ScheduleImpl(id, jobId, instantiation);
  }

  @Override
  public String job() {
    return job;
  }

  @Override
  public Set<CloudiatorProcess> processes() {
    return ImmutableSet.copyOf(processes);
  }

  @Override
  public Instantiation instantiation() {
    return instantiation;
  }

  @Override
  public Schedule addProcess(CloudiatorProcess cloudiatorProcess) {
    processes.add(cloudiatorProcess);
    return this;
  }

  @Override
  public Schedule addProcesses(Collection<? extends CloudiatorProcess> processes) {
    this.processes.addAll(processes);
    return null;
  }

  @Override
  public String id() {
    return id;
  }
}
