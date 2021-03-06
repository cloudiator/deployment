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

import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.graph.JobGraph;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by daniel on 19.06.16.
 */
public class NoCycleInMandatoryCommunicationValidator extends AbstractModelValidator<Job> {

  @Override
  public void inspect(Job job) {
    final JobGraph graph = JobGraph.of(job);
    if (graph.hasCycle()) {
      addValidationMessage(ValidationMessage.of(String.format(
          "Found at least one loop in the mandatory communication graph. %s are participants of the loop. Use optional communication links to break the loop.",
          StringUtils.join(graph.cycles(), ",")), ValidationMessage.Type.ERROR));
    }
  }
}
