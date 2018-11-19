package io.github.cloudiator.deployment.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import java.util.Iterator;
import java.util.Set;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Created by daniel on 17.06.16.
 */
public class JobGraph {

  private final DirectedPseudograph<Task, CommunicationEdge> taskGraph;
  private final DirectedPseudograph<Task, CommunicationEdge>
      mandatoryTaskGraph;

  private JobGraph(Job job) {
    taskGraph = GraphFactory.of(job);
    mandatoryTaskGraph = GraphFactory.of(job, true);
  }

  public static JobGraph of(Job job) {
    checkNotNull(job);
    return new JobGraph(job);
  }

  private CycleDetector<Task, CommunicationEdge> cycleDetector() {
    return new CycleDetector<>(mandatoryTaskGraph);
  }

  public Iterator<Task> evaluationOrder() {
    return new TopologicalOrderIterator<>(
        mandatoryTaskGraph);
  }

  public boolean hasCycle() {
    return cycleDetector().detectCycles();
  }

  public Set<Task> cycles() {
    return cycleDetector().findCycles();
  }

  public JsonNode toJson() {
    final ObjectNode objectNode = new ObjectMapper().createObjectNode().with("elements");
    final ArrayNode nodes = objectNode.putArray("nodes");
    this.taskGraph.vertexSet().forEach(task -> {
      final ObjectNode vertex = nodes.addObject();
      vertex.with("data").put("id", task.name())
          .put("name", task.name());
    });
    final ArrayNode edges = objectNode.putArray("edges");
    this.taskGraph.edgeSet().forEach(communicationEdge -> {
      final ObjectNode edge = edges.addObject();
      edge.with("data").put("id", communicationEdge.id())
          .put("source", taskGraph.getEdgeSource(communicationEdge).name())
          .put("target", taskGraph.getEdgeTarget(communicationEdge).name());
      if (communicationEdge.isMandatory()) {
        edge.put("classes", "mandatory");
      }
    });

    return objectNode;
  }

  private static class CommunicationEdge extends DefaultEdge {

    private final Job job;
    private final Communication communication;

    private CommunicationEdge(Job job, Communication communication) {
      this.communication = communication;
      this.job = job;
    }

    public Communication communication() {
      return communication;
    }

    public String id() {
      String concat = communication.portProvided() + communication.portRequired();
      return Hashing.md5().newHasher().putString(concat, Charsets.UTF_8).hash().toString();
    }

    public boolean isMandatory() {
      return job.requiredPort(communication).isMandatory();
    }
  }


  private static class GraphFactory {

    public static DirectedPseudograph<Task, CommunicationEdge> of(
        Job job, boolean onlyMandatory) {

      DirectedPseudograph<Task, CommunicationEdge> componentGraph =
          new DirectedPseudograph<>(CommunicationEdge.class);
      job.tasks().forEach(componentGraph::addVertex);

      job.communications().stream()
          .filter(communication -> !onlyMandatory || job.requiredPort(communication).isMandatory())
          .forEach(
              communication -> componentGraph
                  .addEdge(job.providingTask(communication), job.requiredTask(communication),
                      new CommunicationEdge(job, communication)));
      return componentGraph;
    }

    public static DirectedPseudograph<Task, CommunicationEdge> of(
        Job job) {
      return GraphFactory.of(job, false);
    }
  }

}
