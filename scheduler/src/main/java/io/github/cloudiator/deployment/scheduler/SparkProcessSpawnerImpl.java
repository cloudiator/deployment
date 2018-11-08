package io.github.cloudiator.deployment.scheduler;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
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
    }catch (IllegalArgumentException e){
      LOGGER.debug("Provided task does not contain a SparkInterface! Skipping SparkProcessSpawner!");
      return false;
    }


  }

  @Override
  public CloudiatorProcess spawn(String userId, String schedule, Job job, Task task, Node node) {
    LOGGER.info(String
        .format("%s is spawning a new Spark process for user: %s, Schedule %s, Task %s on Node %s", this,
            userId, schedule, task, node));

    final SparkProcess sparkProcess = SparkProcess.newBuilder()
        .setSchedule(schedule)
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_MESSAGE_CONVERTER.apply(node)).setTask(task.name()).build();
    final CreateSparkProcessRequest processRequest = CreateSparkProcessRequest.newBuilder()
        .setSpark(sparkProcess).setUserId(userId).build();

    SettableFutureResponseCallback<SparkProcessCreatedResponse, CloudiatorProcess> futureResponseCallback = SettableFutureResponseCallback
        .create(
            sparkProcessCreatedResponse -> PROCESS_MESSAGE_CONVERTER
                .apply(sparkProcessCreatedResponse.getProcess()));

    processService.createSparkProcessAsync(processRequest, futureResponseCallback);

    try {
      return futureResponseCallback.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(
          String.format("%s got interrupted while waiting for result", this));
    } catch (ExecutionException e) {
      throw new IllegalStateException("Error while creating Spark process.", e.getCause());
    }
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
