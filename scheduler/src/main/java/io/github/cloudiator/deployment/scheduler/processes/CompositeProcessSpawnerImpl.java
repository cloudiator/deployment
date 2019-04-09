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

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 07.11.2018.
 */
public class CompositeProcessSpawnerImpl implements ProcessSpawner {

  private final Set<ProcessSpawner> processSpawners;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeProcessSpawnerImpl.class);

  @Inject
  public CompositeProcessSpawnerImpl(
      Set<ProcessSpawner> strategies) {
    this.processSpawners = strategies;
  }

  @Override
  public boolean supports(Task task) {
    return true;
  }

  @Override
  public ProcessGroup spawn(String userId, String schedule, Job job, Task task,
      Set<Node> nodes) {

    LOGGER.debug("Using CompositeProcessSpawner to determine correct ProcessSpawner");

    for (ProcessSpawner processSpawner : processSpawners) {
      if (processSpawner.supports(task)) {
        LOGGER.info(String.format("Using processSpawner %s to spawn task %s.",
            processSpawner, task));

        return processSpawner.spawn(userId, schedule, job, task, nodes);
      }
    }
    throw new IllegalStateException(String
        .format("None of the found process spawners [%s] supports the task %s.",
            Joiner.on(",").join(processSpawners),
            task));
  }


}
