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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcessBuilder;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.SimulationInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.scheduler.simulation.SimulationContext;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimulationProcessSpawner implements ProcessSpawner {

  private final SimulationContext simulationContext;

  @Inject
  public SimulationProcessSpawner(
      SimulationContext simulationContext) {
    this.simulationContext = simulationContext;
  }

  @Override
  public boolean supports(TaskInterface taskInterface) {
    return taskInterface instanceof SimulationInterface;
  }

  private String generateEndpoint(String id) {
    return "http://" + id + ".cloudiator.org";
  }

  @Override
  public CloudiatorSingleProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Node node) throws ProcessSpawningException {

    final String id = UUID.randomUUID().toString();

    final CloudiatorSingleProcess process = CloudiatorSingleProcessBuilder.create().startNow()
        .node(node.id()).userId(userId)
        .scheduleId(schedule.id()).state(
            ProcessState.RUNNING).taskName(task.name()).id(id).type(Type.SIMULATION)
        .taskInterface(taskInterface.getClass().getCanonicalName()).originId(id)
        .endpoint(generateEndpoint(id)).build();

    simulationContext.storeProcess(process);

    return process;
  }

  @Override
  public CloudiatorClusterProcess spawn(String userId, Schedule schedule, Job job, Task task,
      TaskInterface taskInterface, Set<Node> nodes) throws ProcessSpawningException {

    final String id = UUID.randomUUID().toString();

    final CloudiatorClusterProcess process = CloudiatorClusterProcessBuilder.create().startNow()
        .addAllNodes(nodes.stream().map(Identifiable::id).collect(
            Collectors.toSet())).userId(userId)
        .scheduleId(schedule.id()).state(
            ProcessState.RUNNING).taskName(task.name()).id(id).type(Type.SIMULATION)
        .taskInterface(taskInterface.getClass().getCanonicalName()).originId(id)
        .endpoint(generateEndpoint(id)).build();

    simulationContext.storeProcess(process);

    return process;
  }
  
}
