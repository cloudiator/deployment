package io.github.cloudiator.deployment.scheduler;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.domain.ProcessGroups;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Process.CreateSparkProcessRequest;
import org.cloudiator.messages.Process.SparkProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.SparkProcess;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 07.11.2018.
 */
public class SparkProcessSpawnerImpl implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();
  private static final NodeGroupMessageToNodeGroup NODE_GROUP_MESSAGE_TO_NODE_GROUP = new NodeGroupMessageToNodeGroup();
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(LanceProcessSpawnerImpl.class);

  @Inject
  public SparkProcessSpawnerImpl(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(Task task) {

    try {
      task.interfaceOfType(SparkInterface.class);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER
          .debug("Provided task does not contain a SparkInterface! Skipping SparkProcessSpawner!");
      return false;
    }


  }

  @Override
  public ProcessGroup spawn(String userId, String schedule, Job job, Task task,
      NodeGroup nodeGroup) {

    //TODO: check for flag which indicates process mapping, one to one or one to many
    //TODO: for now only one to many is supported until flag is available


    //wait until all processes are spawned
    try {

      LOGGER.info(String
          .format("%s is spawning a new Spark process for user: %s, Schedule %s, Task %s on Node %s",
              this,
              userId, schedule, task, nodeGroup));



      final SparkProcess sparkProcess = SparkProcess.newBuilder()
          .setSchedule(schedule)
          .setJob(JOB_CONVERTER.applyBack(job))
          .setNodeGroup(NODE_GROUP_MESSAGE_TO_NODE_GROUP.applyBack(nodeGroup))
          .setTask(task.name()).build();
      final CreateSparkProcessRequest processRequest = CreateSparkProcessRequest.newBuilder()
          .setSpark(sparkProcess).setUserId(userId).build();

      SettableFutureResponseCallback<SparkProcessCreatedResponse, CloudiatorProcess> futureResponseCallback = SettableFutureResponseCallback
          .create(
              sparkProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                  .apply(sparkProcessCreatedResponse.getProcess()));

      processService.createSparkProcessAsync(processRequest, futureResponseCallback);

      CloudiatorProcess spawnedSparkProcess = futureResponseCallback.get();

      ProcessGroup processGroup = ProcessGroups.ofSingle(spawnedSparkProcess);

      return processGroup;


    } catch (InterruptedException e) {
      LOGGER.error("Spawn Spark Process Execution got interrupted. Stopping.");
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOGGER.error("Error while waiting for LanceProcess to spawn!" ,e);
      throw  new IllegalStateException(e);
    }

    return null;
  }







  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
