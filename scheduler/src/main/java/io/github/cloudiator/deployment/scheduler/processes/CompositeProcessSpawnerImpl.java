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

package io.github.cloudiator.deployment.scheduler.processes;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.security.VariableContextFactory;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 07.11.2018.
 */
public class CompositeProcessSpawnerImpl implements ProcessSpawner {

  private final Set<ProcessSpawner> processSpawners;
  private final VariableContextFactory variableContextFactory;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeProcessSpawnerImpl.class);

  @Inject
  public CompositeProcessSpawnerImpl(
      Set<ProcessSpawner> strategies,
      VariableContextFactory variableContextFactory) {
    this.processSpawners = strategies;
    this.variableContextFactory = variableContextFactory;
  }

  private TaskInterface decorate(String userId, TaskInterface taskInterface) {
    return taskInterface.decorateVariables(variableContextFactory.create(userId));
  }

  private ProcessSpawner determineSpawner(TaskInterface taskInterface) {

    LOGGER.debug(String
        .format("%s determines correct ProcessSpawner for interface %s", this, taskInterface));

    checkState(supports(taskInterface), String
        .format("%s does not support spawning tasks with interface %s.", this, taskInterface));

    for (ProcessSpawner processSpawner : processSpawners) {
      if (processSpawner.supports(taskInterface)) {
        LOGGER.info(String.format("Using processSpawner %s to spawn taskInterface %s.",
            processSpawner, taskInterface));
        return processSpawner;
      }
    }

    throw new AssertionError(
        "Illegal condition bias between supports and determineSpawner. Supports states that a process spawner exists, but none was found.");
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    for (ProcessSpawner processSpawner : processSpawners) {
      if (processSpawner.supports(taskInterface)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public CloudiatorSingleProcess spawn(String userId, String schedule, Job job, Task task,
      TaskInterface taskInterface, Node node) throws ProcessSpawningException {

    return determineSpawner(taskInterface)
        .spawn(userId, schedule, job, task, decorate(userId, taskInterface), node);
  }

  @Override
  public CloudiatorClusterProcess spawn(String userId, String schedule, Job job, Task task,
      TaskInterface taskInterface, Set<Node> nodes) throws ProcessSpawningException {

    return determineSpawner(taskInterface)
        .spawn(userId, schedule, job, task, decorate(userId, taskInterface), nodes);

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("processSpawners", processSpawners).toString();
  }
}
