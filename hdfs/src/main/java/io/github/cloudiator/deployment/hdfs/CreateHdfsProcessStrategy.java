package io.github.cloudiator.deployment.hdfs;

import com.google.gson.Gson;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.deployment.config.Constants;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.HdfsInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.InstallationEntities.Installation;
import org.cloudiator.messages.InstallationEntities.Installation.Builder;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.InstallationRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateHdfsProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateHdfsProcessStrategy.class);

  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  private static final String HDFS_ARGUMENT_DELIMITER = ",";


  /**
   * Hdfs Default Settings
   * currently adding no default settings
   */
  

  private InstallationRequestService installationRequestService;

  @Named(Constants.INSTALL_MELODIC_TOOLS)
  @Inject(optional = true)
  boolean installMelodicTools = false;

  @Inject
  CreateHdfsProcessStrategy(InstallationRequestService installationRequestService) {
    this.installationRequestService = installationRequestService;
  }


  private void installHdfsDataNodes(String userId, Set<Node> nodes) {

    for (Node node : nodes) {

      LOGGER.debug("Installing Docker and Hdfs data node on node: " + node.id());

      final Builder builder = Installation.newBuilder()
          .setNode(NODE_MESSAGE_CONVERTER.apply(node))
          .addTool(Tool.DOCKER)
          .addTool(Tool.HDFS_DATA);

      if (installMelodicTools) {
        builder
            .addTool(Tool.ALLUXIO_CLIENT)
            .addTool(Tool.DLMS_AGENT);
      }

      final InstallationRequest installationRequest = InstallationRequest.newBuilder()
          .setUserId(userId).setInstallation(builder.build()).build();

      final SettableFutureResponseCallback<InstallationResponse, InstallationResponse> futureResponseCallback = SettableFutureResponseCallback
          .create();

      installationRequestService
          .createInstallationRequestAsync(installationRequest, futureResponseCallback);
      try {
        futureResponseCallback.get();
      } catch (InterruptedException e) {
        throw new IllegalStateException(
            "Docker and Hdfs data node installation was interrupted during installation request.",
            e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Error during Docker and Hdfd data node installation",
            e.getCause());
      }

      LOGGER.debug("Finished Docker and HDFS data node installation on node: " + node.id());
    }


  }



  public void executeClusterDeployment(String userId, Set<Node> nodes) {

    LOGGER.info(String
        .format("Deploying a new Hdfs cluster for user: %s  on nodes %s",
            userId, nodes));

    try {

      LOGGER.debug("Triggering Hdfs data node installations...");
      this.installHdfsDataNodes(userId, nodes);


      LOGGER.debug("Successfully deployed HDFS cluster!");



    } catch (Exception e) {
      throw new IllegalStateException("Could not deploy HDFS cluster on nodes " + nodes, e);
    }

  }


  public CloudiatorProcess executeJobSubmission(String userId, String schedule, Task task,
      HdfsInterface hdfsInterface,Set<Node> nodes) {

    LOGGER.info(String
        .format("Submitting new HdfsJobSubmission for user: %s, schedule %s, task %s on nodes %s",
            userId, schedule, task, nodes));

    try {

      UUID uuid = UUID.randomUUID();
      String temporaryHdfsProcessUid = uuid.toString();

      return CloudiatorClusterProcessBuilder.create().id(temporaryHdfsProcessUid)
          .originId(temporaryHdfsProcessUid)
          .userId(userId)
          .type(Type.HDFS)
          .taskInterface(HdfsInterface.class.getCanonicalName())
          .state(ProcessState.RUNNING)
          .addAllNodes(nodes.stream().map(Identifiable::id).collect(Collectors.toList()))
          .taskName(task.name()).scheduleId(schedule).startNow().build();

    } catch (Exception e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }

  }

}
