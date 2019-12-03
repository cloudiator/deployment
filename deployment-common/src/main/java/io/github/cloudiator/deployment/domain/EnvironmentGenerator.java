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

package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import io.github.cloudiator.deployment.graph.Graphs;
import io.github.cloudiator.deployment.graph.ScheduleGraph;
import io.github.cloudiator.deployment.graph.ScheduleGraph.CommunicationInstanceEdge;

public class EnvironmentGenerator {

  private static final String PUBLIC_DOWNSTREAM = "PUBLIC_%s";
  private static final String PROCESS_ID = "PROCESS_ID";
  private static final String NODE_IDS = "NODE_IDS";
  private static final String FINISH_ENDPOINT = "FINISH_ENDPOINT";
  private static final String FINISH_ENDPOINT_TEMPLATE = "%s/process/%s/%s";

  private final Job job;
  private final Schedule schedule;
  private final String apiEndpoint;

  private EnvironmentGenerator(Job job, Schedule schedule, String apiEndpoint) {
    this.job = job;
    this.schedule = schedule;
    this.apiEndpoint = apiEndpoint;
  }

  public static EnvironmentGenerator of(Job job, Schedule schedule,
      String apiEndpoint) {
    checkNotNull(job, "job is null");
    checkNotNull(schedule, "schedule is null");
    checkNotNull(apiEndpoint, "apiEndpoint is null");

    return new EnvironmentGenerator(job, schedule, apiEndpoint);
  }


  public Environment generate(CloudiatorProcess cloudiatorProcess) {

    Environment environment = new Environment();
    final ScheduleGraph scheduleGraph = Graphs.scheduleGraph(schedule, job);

    checkArgument(schedule.hasProcess(cloudiatorProcess),
        String.format("Process %s does not belong to schedule %s.", cloudiatorProcess, schedule));

    environment.put(PROCESS_ID, cloudiatorProcess.id());
    environment.put(NODE_IDS, Joiner.on(",").join(cloudiatorProcess.nodes()));

    environment.put(FINISH_ENDPOINT, String
        .format(FINISH_ENDPOINT_TEMPLATE, apiEndpoint, cloudiatorProcess.id(),
            cloudiatorProcess.secret().orElse(null)));

    for (CloudiatorProcess dependency : scheduleGraph.getDependencies(cloudiatorProcess)) {

      final CommunicationInstanceEdge edge = scheduleGraph.getEdge(dependency, cloudiatorProcess);
      if (dependency.endpoint().isPresent()) {
        environment.put(String.format(PUBLIC_DOWNSTREAM, edge.getCommunication().portRequired()),
            dependency.endpoint().get());
      }
    }

    return environment;

  }

}
