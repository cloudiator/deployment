/*
 * Copyright 2017 University of Ulm
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.cloudiator.matchmaking.domain.Optimization;
import org.cloudiator.matchmaking.domain.Requirement;

/**
 * Created by daniel on 13.02.17.
 */
public class JobBuilder {

  private String id;
  private String userId;
  private String name;
  private Set<Task> tasks = new HashSet<>();
  private Set<Communication> communications = new HashSet<>();
  private Set<Requirement> requirements = new HashSet<>();
  private Optimization optimization;

  private JobBuilder() {

  }

  public static JobBuilder newBuilder() {
    return new JobBuilder();
  }

  public JobBuilder generateId() {
    this.id = UUID.randomUUID().toString();
    return this;
  }

  public JobBuilder id(String id) {
    this.id = id;
    return this;
  }

  public JobBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  public JobBuilder name(String name) {
    this.name = name;
    return this;
  }

  public JobBuilder addTask(Task task) {
    this.tasks.add(task);
    return this;
  }

  public JobBuilder addTasks(
      Collection<? extends Task> tasks) {
    this.tasks.addAll(tasks);
    return this;
  }

  public JobBuilder addCommunication(Communication communication) {
    this.communications.add(communication);
    return this;
  }

  public JobBuilder addCommunications(Set<? extends Communication> communications) {
    this.communications.addAll(communications);
    return this;
  }

  public JobBuilder addRequirement(Requirement requirement) {
    this.requirements.add(requirement);
    return this;
  }

  public JobBuilder addRequirements(
      Collection<? extends Requirement> requirements) {
    this.requirements.addAll(requirements);
    return this;
  }

  public JobBuilder optimization(Optimization optimization) {
    this.optimization = optimization;
    return this;
  }

  public Job build() {
    return new JobImpl(id, userId, name, tasks, communications, requirements, optimization);
  }


}
