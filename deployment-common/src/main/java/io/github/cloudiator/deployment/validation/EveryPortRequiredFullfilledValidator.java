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

import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.validation.ValidationMessage.Type;

/**
 * Created by daniel on 19.06.16.
 */
public class EveryPortRequiredFullfilledValidator extends AbstractModelValidator<Job> {

  private static final String ERROR_MESSAGE = "Task %s has required port %s but within job %s no communication is attached to this port thus the requirement is never fulfilled.";

  @Override
  public void inspect(Job job) {
    for (Task task : job.tasks()) {
      for (PortRequired portRequired : task.requiredPorts()) {
        if (!portFulfilled(job, portRequired)) {
          addValidationMessage(ValidationMessage
              .of(String.format(ERROR_MESSAGE, task, portRequired, job), Type.ERROR));
        }
      }
    }
  }

  private static boolean portFulfilled(Job job, PortRequired portRequired) {
    for (Communication communication : job.communications()) {
      if (communication.portRequired().equals(portRequired.name())) {
        return true;
      }
    }
    return false;
  }
}
