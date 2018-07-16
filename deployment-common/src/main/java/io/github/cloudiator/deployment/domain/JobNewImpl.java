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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class JobNewImpl implements JobNew {

  private final Set<Task> tasks;
  private final Set<Communication> communications;
  private final String name;

  JobNewImpl(String name, Set<Task> tasks,
      Set<Communication> communications) {

    checkNotNull(name, "name is null");
    checkArgument(!name.isEmpty(), "name is empty");
    checkNotNull(tasks, "tasks is null");
    checkNotNull(communications, "communications is null");

    this.name = name;
    this.tasks = ImmutableSet.copyOf(tasks);
    this.communications = ImmutableSet.copyOf(communications);
  }

  @Override
  public Set<Task> tasks() {
    return tasks;
  }

  @Override
  public Set<Communication> communications() {
    return communications;
  }

  @Override
  public String name() {
    return name;
  }

  protected MoreObjects.ToStringHelper stringHelper() {
    return MoreObjects.toStringHelper(this).add("name", name)
        .add("communications", Joiner.on(",").join(communications))
        .add("tasks", Joiner.on(",").join(tasks));
  }

  @Override
  public String toString() {
    return stringHelper().toString();
  }
}
