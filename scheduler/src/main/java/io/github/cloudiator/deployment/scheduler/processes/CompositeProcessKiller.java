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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.scheduler.exceptions.ProcessDeletionException;
import java.util.Set;

public class CompositeProcessKiller implements ProcessKiller {

  private final Set<ProcessKiller> processKillers;

  @Inject
  public CompositeProcessKiller(
      Set<ProcessKiller> processKillers) {
    this.processKillers = processKillers;
  }

  @Override
  public boolean supports(CloudiatorProcess cloudiatorProcess) {

    for (ProcessKiller processKiller : processKillers) {
      if (processKiller.supports(cloudiatorProcess)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void kill(CloudiatorProcess cloudiatorProcess) throws ProcessDeletionException {

    checkState(supports(cloudiatorProcess),
        String.format("%s does not support killing the process %s.", this, cloudiatorProcess));

    checkState(cloudiatorProcess.state().isRemovable(),
        String.format("Process is in state %s and can not be removed.", cloudiatorProcess.state()));

    for (ProcessKiller processKiller : processKillers) {
      if (processKiller.supports(cloudiatorProcess)) {
        processKiller.kill(cloudiatorProcess);
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("processKillers", processKillers).toString();
  }
}
