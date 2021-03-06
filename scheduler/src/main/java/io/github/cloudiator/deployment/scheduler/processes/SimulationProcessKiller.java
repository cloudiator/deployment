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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.scheduler.exceptions.ProcessDeletionException;
import io.github.cloudiator.deployment.scheduler.simulation.SimulationContext;
import java.util.NoSuchElementException;

public class SimulationProcessKiller implements ProcessKiller {

  private final SimulationContext simulationContext;

  @Inject
  public SimulationProcessKiller(
      SimulationContext simulationContext) {
    this.simulationContext = simulationContext;
  }

  @Override
  public boolean supports(CloudiatorProcess cloudiatorProcess) {
    return cloudiatorProcess.type().equals(Type.SIMULATION);
  }

  @Override
  public void kill(CloudiatorProcess cloudiatorProcess) throws ProcessDeletionException {
    try {
      simulationContext.delete(cloudiatorProcess.id());
    } catch (NoSuchElementException e) {
      throw new ProcessDeletionException(e);
    }

  }
}
