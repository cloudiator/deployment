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

package io.github.cloudiator.deployment.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.validation.ValidationMessage.Type;

public class EveryTaskHasAnInterface extends AbstractModelValidator<Job> {

  private static final String ERROR_MESSAGE = "Task %s of job %s has no defined interface.";

  @Override
  public void inspect(Job job) {
    checkNotNull(job, "job is null");

    for (Task task : job.tasks()) {
      if (task.interfaces().isEmpty()) {

        addValidationMessage(ValidationMessage.of(String
            .format(ERROR_MESSAGE, task, job), Type.ERROR));

      }
    }
  }

}
