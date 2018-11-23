package io.github.cloudiator.deployment.graph;

import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.domain.Node;
import java.util.Set;

/**
 * Created by daniel on 17.02.17.
 */
public class Graphs {

  private Graphs() {
    throw new AssertionError("Do not instantiate");
  }

  public static JobGraph jobGraph(Job job) {
    return JobGraph.of(job);
  }

  public static ScheduleGraph applicationInstanceGraph(Schedule schedule, Job job,
      Set<Node> nodes) {
    return new ScheduleGraph(schedule, job, nodes);
  }

}
