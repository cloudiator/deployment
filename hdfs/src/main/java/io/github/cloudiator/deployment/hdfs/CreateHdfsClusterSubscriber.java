package io.github.cloudiator.deployment.hdfs;

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


public class CreateHdfsClusterSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateHdfsClusterSubscriber.class);
  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final CreateHdfsProcessStrategy createHdfsProcessStrategy;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public CreateHdfsClusterSubscriber(
      ProcessService processService,
      CreateHdfsProcessStrategy createHdfsProcessStrategy,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.createHdfsProcessStrategy = createHdfsProcessStrategy;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {

    LOGGER.debug("Create HdfsClusterSubscriber started and waiting for requests...");

    processService.subscribeCreateHdfsClusterRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();


            Set<Node> nodes = content.getNodes().getNodesList().stream()
                .map(NODE_MESSAGE_CONVERTER::applyBack).collect(
                    Collectors.toSet());



           createHdfsProcessStrategy.executeClusterDeployment(userId,nodes);

           final hdfsClusterCreatedResponse hdfsClusterCreatedResponse = hdfsClusterCreatedResponse.newBuilder().build();

           messageInterface.reply(id, hdfsClusterCreatedResponse);


          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(HdfsClusterCreatedResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });

  }
}
