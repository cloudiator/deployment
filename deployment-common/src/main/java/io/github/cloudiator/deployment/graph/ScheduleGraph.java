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
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * Created by daniel on 15.07.16.
 */
public class ScheduleGraph {

  private final DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> scheduleGraph;
  private final Set<Node> nodes;

  ScheduleGraph(Schedule schedule, Job job, Set<Node> nodes) {
    scheduleGraph = GraphFactory.of(schedule, job);
    this.nodes = nodes;
  }

  private static class CommunicationInstanceEdge extends DefaultEdge {

  }


  private static class CommunicationInstanceEdgeFactory
      implements EdgeFactory<CloudiatorProcess, CommunicationInstanceEdge> {

    @Override
    public CommunicationInstanceEdge createEdge(CloudiatorProcess sourceVertex,
        CloudiatorProcess targetVertex) {
      return new CommunicationInstanceEdge();
    }
  }

  private Node nodeFor(CloudiatorProcess cloudiatorProcess) {
    return nodes.stream().filter(new Predicate<Node>() {
      @Override
      public boolean test(Node node) {
        return node.id().equals(cloudiatorProcess.nodeId());
      }
    }).collect(StreamUtil.getOnly()).orElseThrow(() -> new IllegalStateException(String.format(
        "Node set %s does not contain the node with id %s required for cloudiator process %s.",
        this.nodes, cloudiatorProcess.nodeId(), cloudiatorProcess)));
  }

  public JsonNode toJson() {
    final ObjectNode objectNode = new ObjectMapper().createObjectNode().with("elements");
    final ArrayNode nodes = objectNode.putArray("nodes");
    this.scheduleGraph.vertexSet().forEach(process -> {
      final ObjectNode vertex = nodes.addObject();
      vertex.with("data").put("id", process.id()).put("type", "INSTANCE")
          .put("name", process.taskId())
          .put("state", process.state().toString())
          .put("parent", process.nodeId());
    });
    //add virtual machines as compound nodes
    this.scheduleGraph.vertexSet().stream().map(this::nodeFor).distinct()
        .forEach(node -> {
          final ObjectNode compound = nodes.addObject();
          compound.with("data").put("id", node.id()).put("type", "VM")
              .put("name", node.name())
              .put("state", "UNKNOWN").put("publicIp",
              node.connectTo().ip());
        });
    final ArrayNode edges = objectNode.putArray("edges");
    this.scheduleGraph.edgeSet().forEach(communicationEdge -> {
      final ObjectNode edge = edges.addObject();
      edge.with("data").put("id", new Random().nextInt())
          .put("source", scheduleGraph.getEdgeSource(communicationEdge).id())
          .put("target", scheduleGraph.getEdgeTarget(communicationEdge).id());
    });

    return objectNode;
  }


  private static class GraphFactory {

    public static DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> of(
        Schedule schedule, Job job) {

      DirectedPseudograph<CloudiatorProcess, CommunicationInstanceEdge> instanceGraph =
          new DirectedPseudograph<>(new CommunicationInstanceEdgeFactory());

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
                instanceGraph.addEdge(cloudiatorProcess, otherProcess);
              }
            }
          }
        }
      });

      return instanceGraph;
    }
  }

}
