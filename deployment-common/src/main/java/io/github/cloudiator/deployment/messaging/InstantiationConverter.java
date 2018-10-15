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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import org.cloudiator.messages.entities.ProcessEntities;

public class InstantiationConverter implements
    TwoWayConverter<ProcessEntities.Instantiation, Instantiation> {

  public static final InstantiationConverter INSTANCE = new InstantiationConverter();

  private InstantiationConverter() {
  }

  @Override
  public ProcessEntities.Instantiation applyBack(Instantiation instantiation) {

    switch (instantiation) {
      case MANUAL:
        return ProcessEntities.Instantiation.MANUAL;
      case AUTOMATIC:
        return ProcessEntities.Instantiation.AUTOMATIC;
      default:
        throw new AssertionError("Unknown instantiation " + instantiation);
    }
  }

  @Override
  public Instantiation apply(ProcessEntities.Instantiation instantiation) {
    switch (instantiation) {
      case AUTOMATIC:
        return Instantiation.AUTOMATIC;
      case MANUAL:
        return Instantiation.MANUAL;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Illegal instantiation " + instantiation);
    }
  }
}
