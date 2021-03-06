/*
 * Copyright (c) 2014-2016 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.deployment.graph;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.EdgeReversedGraph;

/**
 * Created by daniel on 15.07.16.
 */
public class ScheduleGraph {

  private final DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> scheduleGraph;

  ScheduleGraph(Schedule schedule, Job job) {
    scheduleGraph = GraphFactory.of(schedule, job);
  }

  public static class CommunicationInstanceEdge extends DefaultEdge {

    private final Communication communication;

    public CommunicationInstanceEdge(
        Communication communication) {
      this.communication = communication;
    }

    public CloudiatorProcess source() {
      return (CloudiatorProcess) super.getSource();
    }

    public CloudiatorProcess target() {
      return (CloudiatorProcess) super.getTarget();
    }

    public Communication getCommunication() {
      return communication;
    }
  }

  private String generateIdForEdge(CommunicationInstanceEdge edge) {
    @SuppressWarnings("UnstableApiUsage") final Funnel<CommunicationInstanceEdge> funnel = (from, into) -> {
      into.putString(from.source().id(), Charsets.UTF_8);
      into.putString(from.target().id(), Charsets.UTF_8);
    };
    @SuppressWarnings("UnstableApiUsage") final HashFunction hashFunction = Hashing.md5();
    return hashFunction.hashObject(edge, funnel).toString();
  }

  public JsonNode toJson() {
    final ObjectNode objectNode = new ObjectMapper().createObjectNode().with("elements");
    final ArrayNode processes = objectNode.putArray("processes");
    this.scheduleGraph.vertexSet().forEach(process -> {
      final ObjectNode vertex = processes.addObject();
      final ObjectNode data = vertex.with("data");
      data.put("id", process.id())
          .put("task", process.taskId()).put("state", process.state().toString());
      final ArrayNode ipAddresses = data.putArray("ipAddresses");
      for (IpAddress ipAddress : process.ipAddresses()) {
        ipAddresses.add(ipAddress.ip());
      }
    });
    final ArrayNode edges = objectNode.putArray("edges");
    this.scheduleGraph.edgeSet().forEach(communicationEdge -> {
      final ObjectNode edge = edges.addObject();
      edge.with("data").put("id", generateIdForEdge(communicationEdge))
          .put("source", communicationEdge.source().id())
          .put("target", communicationEdge.target().id());
    });

    return objectNode;
  }

  public Graph<CloudiatorProcess, CommunicationInstanceEdge> reverse() {
    return new EdgeReversedGraph<>(scheduleGraph);
  }

  public List<CloudiatorProcess> getDependentProcesses(CloudiatorProcess cloudiatorProcess) {
    return org.jgrapht.Graphs.successorListOf(scheduleGraph, cloudiatorProcess);
  }

  public List<CloudiatorProcess> getDependencies(CloudiatorProcess cloudiatorProcess) {
    return org.jgrapht.Graphs.predecessorListOf(scheduleGraph, cloudiatorProcess);
  }

  public CommunicationInstanceEdge getEdge(CloudiatorProcess source, CloudiatorProcess target) {
    return scheduleGraph.getEdge(source, target);
  }

  private static class GraphFactory {

    public static DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> of(
        Schedule schedule, Job job) {

      DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> instanceGraph =
          new DirectedPseudograph<>(CommunicationInstanceEdge.class);

      schedule.processes().forEach(instanceGraph::addVertex);
      schedule.processes().forEach(cloudiatorProcess -> {
        final String taskId = cloudiatorProcess.taskId();

        final Task task = job.getTask(taskId).orElseThrow(() -> new IllegalStateException(String
            .format("Process belongs to task %s but job %s does not contain this task", taskId,
                job)));

        for (PortProvided providedPort : task.providedPorts()) {
          final Set<Communication> communications = job.attachedCommunications(providedPort);
          for (Communication communication : communications) {
            checkState(communication.portProvided().equals(providedPort.name()), String.format(
                "Communication %s is attached to provided port %s but this port is not the provided port of the communication",
                communication, providedPort));

            final Task requiredTask = job.requiredTask(communication);

            for (CloudiatorProcess otherProcess : schedule.processes()) {
              if (otherProcess.taskId().equals(requiredTask.name())) {
                instanceGraph.addEdge(cloudiatorProcess, otherProcess,
                    new CommunicationInstanceEdge(communication));
              }
            }
          }
        }
      });

      return instanceGraph;
    }
  }

}
