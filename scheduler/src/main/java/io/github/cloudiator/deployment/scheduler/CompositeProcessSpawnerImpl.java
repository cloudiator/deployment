package io.github.cloudiator.deployment.scheduler;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 07.11.2018.
 */
public class CompositeProcessSpawnerImpl implements ProcessSpawner {

  private final Set<ProcessSpawner> processSpawners;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeProcessSpawnerImpl.class);

  @Inject
  public CompositeProcessSpawnerImpl(
      Set<ProcessSpawner> strategies) {
    this.processSpawners = strategies;
  }

  @Override
  public boolean supports(Task task) {
    return true;
  }

  @Override
  public Future<CloudiatorProcess> spawn(String userId, String schedule, Job job, Task task,
      Node node) {

    LOGGER.debug("Using CompositeProcessSpawner to determine correct ProcessSpawner");

    for (ProcessSpawner processSpawner : processSpawners) {
      if (processSpawner.supports(task)) {
        LOGGER.info(String.format("Using processSpawner %s to spawn task %s.",
            processSpawner, task));

        return processSpawner.spawn(userId, schedule, job, task, node);
      }
    }
    throw new IllegalStateException(String
        .format("None of the found process spawners [%s] supports the task %s.",
            Joiner.on(",").join(processSpawners),
            task));
  }


}
