package io.github.cloudiator.deployment.spark;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.SparkClusterCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cloudiator.messages.General.Error;

/**
 * Created by Daniel Seybold on 22.05.2019.
 */
public class CreateSparkClusterSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkClusterSubscriber.class);
  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final CreateSparkProcessStrategy createSparkProcessStrategy;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public CreateSparkClusterSubscriber(
      ProcessService processService,
      CreateSparkProcessStrategy createSparkProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.createSparkProcessStrategy = createSparkProcessStrategy;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {

    LOGGER.debug("Create SparkClusterSubscriber started and waiting for requests...");

    processService.subscribeCreateSparkClusterRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();


            Set<Node> nodes = content.getNodes().getNodesList().stream()
                .map(NODE_MESSAGE_CONVERTER::applyBack).collect(
                    Collectors.toSet());



           createSparkProcessStrategy.executeClusterDeployment(userId,nodes);

           final SparkClusterCreatedResponse sparkClusterCreatedResponse = SparkClusterCreatedResponse.newBuilder().build();

           messageInterface.reply(id, sparkClusterCreatedResponse);


          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(SparkClusterCreatedResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });

  }
}
