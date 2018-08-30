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

import de.uniulm.omi.cloudiator.domain.Identifiable;
import java.util.Collection;
import java.util.Set;
import org.cloudiator.messages.entities.ProcessEntities;

public interface Schedule extends Identifiable {

  enum Instantiation {
    AUTOMATIC,
    MANUAL;

    public static Instantiation valueOf(ProcessEntities.Instantiation instantiation) {
      switch (instantiation) {
        case AUTOMATIC:
          return AUTOMATIC;
        case MANUAL:
          return MANUAL;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unrecognized instantiation " + instantiation);
      }
    }

    public ProcessEntities.Instantiation toMessage() {
      switch (this) {
        case MANUAL:
          return ProcessEntities.Instantiation.MANUAL;
        case AUTOMATIC:
          return ProcessEntities.Instantiation.AUTOMATIC;
        default:
          throw new AssertionError("Unrecognized instantiation " + this);
      }
    }
  }

  String job();

  Set<CloudiatorProcess> processes();

  Instantiation instantiation();

  Schedule addProcess(CloudiatorProcess cloudiatorProcess);

  Schedule addProcesses(Collection<? extends CloudiatorProcess> processes);


}
