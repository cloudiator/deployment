package io.github.cloudiator.deployment.hdfs;

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


public class CreateHdfsProcessSubscriber implements Runnable {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private final CreateHdfsProcessStrategy createHdfsProcessStrategy;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateHdfsProcessSubscriber.class);
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public CreateHdfsProcessSubscriber(
      ProcessService processService,
      CreateHdfsProcessStrategy createHdfsProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.createHdfsProcessStrategy = createHdfsProcessStrategy;
    this.messageInterface = messageInterface;
  }


  @Override
  public void run() {

    LOGGER.debug("CreateHdfsProcessRequestSubscriber started and waiting for requests...");

    processService.subscribeCreateHdfsProcessRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();
            final Job job = JOB_CONVERTER.apply(conten.getHdfs().getJob());
            final String task = content.getHdfs().getTask();

            switch (content.getHdfs().getRunsOnCase()) {

              case NODES:
                break;
              case NODE:
                throw new UnsupportedOperationException(
                    "Running hdfs on single node is currently unsupported.");
              case RUNSON_NOT_SET:
              default:
                throw new AssertionError(
                    "Illegal RunsOn Case " + content.getSpark().getRunsOnCase());
            }

            Set<Node> nodes = content.getHdfs().getNodes().getNodesList().stream()
                .map(NODE_MESSAGE_CONVERTER::applyBack).collect(
                    Collectors.toSet());

            final HdfsInterface hdfsInterface = HdfsInterfaceConverter.INSTANCE
                .apply(content.getHdfs().getHdfsInterface());

            final String schedule = content.getHdfs().getSchedule();

            final CloudiatorProcess cloudiatorProcess = createHdfsProcessStrategy
                .executeJobSubmission(userId, schedule, job.getTask(task).orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Job %s does not contain task %s", job, task))),
                    sparkInterface,
                    nodes);

            final HdfsProcessCreatedResponse hdfsProcessCreatedResponse = HdfsProcessCreatedResponse
                .newBuilder()
                .setProcess(PROCESS_MESSAGE_CONVERTER.applyBack(cloudiatorProcess)).build();

            messageInterface.reply(id, hdfsProcessCreatedResponse);

          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(HdfsProcessCreatedResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });
  }

}
