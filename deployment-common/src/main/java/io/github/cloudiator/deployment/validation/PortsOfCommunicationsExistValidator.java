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

package io.github.cloudiator.deployment.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.validation.ValidationMessage.Type;
import java.util.Optional;

public class PortsOfCommunicationsExistValidator extends AbstractModelValidator<Job> {

  private static final String ERROR_MESSAGE = "Communication %s references port %s of type %s but job %s does not contain this port";

  @Override
  public void inspect(Job job) {
    checkNotNull(job, "job is null");

    for (Communication communication : job.communications()) {

      if (!portExists(job, communication.portProvided(), PortProvided.class)) {
        addValidationMessage(ValidationMessage.of(String
            .format(ERROR_MESSAGE, communication, communication.portProvided(), PortProvided.class,
                job), Type.ERROR));
      }

      if (!portExists(job, communication.portRequired(),
          PortRequired.class)) {
        addValidationMessage(ValidationMessage.of(String
            .format(ERROR_MESSAGE, communication, communication.portRequired(), PortRequired.class,
                job), Type.ERROR));
      }

    }

  }


  public static boolean portExists(Job job, String name, Class<? extends Port> type) {
    for (Task task : job.tasks()) {
      final Optional<? extends Port> port = task.findPort(name, type);
      if (port.isPresent()) {
        return true;
      }
    }
    return false;
  }

}
