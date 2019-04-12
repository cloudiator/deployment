package io.github.cloudiator.deployment.spark;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.messaging.SparkInterfaceConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.Set;
import java.util.stream.Collectors;
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
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private final CreateSparkProcessStrategy createSparkProcessStrategy;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkProcessSubscriber.class);
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

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

            switch (content.getSpark().getRunsOnCase()) {

              case NODES:
                break;
              case NODE:
                throw new UnsupportedOperationException(
                    "Running spark on single node is currently unsupported.");
              case RUNSON_NOT_SET:
              default:
                throw new AssertionError(
                    "Illegal RunsOn Case " + content.getSpark().getRunsOnCase());
            }

            Set<Node> nodes = content.getSpark().getNodes().getNodesList().stream()
                .map(NODE_MESSAGE_CONVERTER::applyBack).collect(
                    Collectors.toSet());

            final SparkInterface sparkInterface = SparkInterfaceConverter.INSTANCE
                .apply(content.getSpark().getSparkInterface());

            final String schedule = content.getSpark().getSchedule();

            final CloudiatorProcess cloudiatorProcess = createSparkProcessStrategy
                .execute(userId, schedule, job.getTask(task).orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Job %s does not contain task %s", job, task))),
                    sparkInterface,
                    nodes);

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
