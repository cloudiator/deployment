package io.github.cloudiator.deployment.spark;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.SparkProcessCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 06.11.2018.
 */
public class CreateSparkProcessSubscriber implements Runnable {

  private final ProcessService processService;
  private final NodeGroupMessageToNodeGroup nodeGroupMessageToNodeGroup = new NodeGroupMessageToNodeGroup();
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private final CreateSparkProcessStrategy createSparkProcessStrategy;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkProcessSubscriber.class);
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private final MessageInterface messageInterface;

  @Inject
  public CreateSparkProcessSubscriber(
      ProcessService processService,
      CreateSparkProcessStrategy createSparkProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.createSparkProcessStrategy = createSparkProcessStrategy;
    this.messageInterface = messageInterface;
  }


  @Override
  public void run() {

    LOGGER.debug("CreateSparkProcessRequestSubscriber started and waiting for requests...");

    processService.subscribeCreateSparkProcessRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();
            final Job job = JOB_CONVERTER.apply(content.getSpark().getJob());
            final String task = content.getSpark().getTask();


            NodeGroup nodeGroupDomain = nodeGroupMessageToNodeGroup
                .apply(content.getSpark().getNodeGroup());



            final String schedule = content.getSpark().getSchedule();

            final CloudiatorProcess cloudiatorProcess = createSparkProcessStrategy
                .execute(userId, schedule, job, job.getTask(task).orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Job %s does not contain task %s", job, task))), nodeGroupDomain);


            final SparkProcessCreatedResponse sparkProcessCreatedResponse = SparkProcessCreatedResponse
                .newBuilder()
                .setProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess)).build();

            messageInterface.reply(id, sparkProcessCreatedResponse);

          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(SparkProcessCreatedResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });
  }

}
