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

import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.graph.JobGraph;
import io.github.cloudiator.deployment.validation.ValidationMessage.Type;
import java.util.List;

public class TaskDoesNotDependOnPeriodicTaskValidator extends AbstractModelValidator<Job> {

  @Override
  public void inspect(Job job) {

    final JobGraph jobGraph = JobGraph.of(job);

    for (Task task : job.tasks()) {

      //get dependencies
      final List<Task> dependencies = jobGraph.getDependencies(task, false);

      for (Task dependency : dependencies) {

        if (dependency.behaviour() instanceof PeriodicBehaviour) {
          addValidationMessage(ValidationMessage
              .of(String.format(
                  "A task may never depend on a task that only runs periodically (PeriodicBehaviour). This is the case for task %s that depends on task %s with %s",
                  task.name(), dependency.name(),
                  dependency.behaviour().getClass().getSimpleName()),
                  Type.ERROR));
        }
      }
    }
  }
}
