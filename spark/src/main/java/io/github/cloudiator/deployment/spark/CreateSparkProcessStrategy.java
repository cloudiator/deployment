package io.github.cloudiator.deployment.spark;

import com.google.gson.Gson;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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

/**
 * Created by Daniel Seybold on 06.11.2018.
 */
public class CreateSparkProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkProcessStrategy.class);

  private final NodeToNodeMessageConverter nodeMessageToNodeConverter = NodeToNodeMessageConverter.INSTANCE;

  private static final String SPARK_ARGUMENT_DELIMITER = ",";

  private InstallationRequestService installationRequestService;

  @Inject
  CreateSparkProcessStrategy(InstallationRequestService installationRequestService) {
    this.installationRequestService = installationRequestService;
  }


  private void installSparkWorkers(String userId, NodeGroup nodeGroup) {

    for (Node node : nodeGroup.getNodes()) {

      //TODO: trigger sync install request and check if installation was successfull
      LOGGER.debug("Installing Docker and Spark Worker on node: " + node.id());

      final Builder builder = Installation.newBuilder()
          .setNode(nodeMessageToNodeConverter.apply(node))
          .addTool(Tool.DOCKER)
          .addTool(Tool.SPARK_WORKER);

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
            "Docker and Spark Worker  installation was interrupted during installation request.",
            e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Error during Docker and Spark Worker installation",
            e.getCause());
      }

      LOGGER.debug("Finished Docker and Spark Worker installation on node: " + node.id());
    }


  }


  private void submitSparkProcessToLivy(Task task) {

    //find SparkInterface
    LOGGER.debug("Submitting Spark process to Livy Server...");
    SparkInterface sparkInterface = task.interfaceOfType(SparkInterface.class);

    if (sparkInterface == null) {
      throw new IllegalStateException(
          "No SparkInterface in TaskInterface set was found! Aborting Spark Process submission!");
    }

    //execute HTTP POST call to Livy Server
    ResponseHandler<String> handler = new BasicResponseHandler();

    CloseableHttpClient client = HttpClients.createDefault();

    HttpPost httpPost = new HttpPost(
        "http://" + Configuration.conf().getString("livy.server") + "/batches");

    LivyBatch livyBatch = buildLivyBatch(sparkInterface);

    Gson gson = new Gson();

    String payload = gson.toJson(livyBatch);

    StringEntity entity = null;
    try {
      entity = new StringEntity(payload);

      httpPost.setEntity(entity);
      httpPost.setHeader("Content-type", "application/json");

      LOGGER.debug("HttpPost: " + httpPost.toString());
      LOGGER.debug("Submit Spark process payload: " + payload);

      CloseableHttpResponse response = client.execute(httpPost);

      int code = response.getStatusLine().getStatusCode();

      if (code != HttpStatus.SC_CREATED) {
        throw new IllegalStateException(
            "Submission of Spark process to livy faild with response code: " + code);
      }

      //TODO: get appId from response (as soon as Livy bug is fixed and appId is properly set)
      String body = handler.handleResponse(response);
      client.close();

      LOGGER.debug("Successfully submitted Spark Process to Livy Server!");


    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Error while creating HTTP Post payload for Livy Server call!", e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    } catch (ClientProtocolException e) {
      LOGGER.error("Error while executing HTTP Post call to Livy Server!", e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    } catch (IOException e) {
      LOGGER.error("Error while handling HTTP Post response from Livy Server!", e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    }


  }

  private static LivyBatch buildLivyBatch(SparkInterface sparkInterface) {

    LivyBatch livyBatch = new LivyBatch();

    /**
     * add mandatory file
     */
    livyBatch.setFile(sparkInterface.file());

    /**
     * add optional class name
     */
    if (sparkInterface.className().isPresent() && !sparkInterface.className().get().isEmpty()) {
      livyBatch.setClassName(sparkInterface.className().get());
    }

    /**
     * add optional argument list
     */
    livyBatch.setArgs(sparkInterface.arguments());

    /**
     * add optional Spark arguments
     */
    if (sparkInterface.sparkArguments().containsKey("proxyUser")) {
      livyBatch.setProxyUser(sparkInterface.sparkArguments().get("proxyUser"));
    }

    if (sparkInterface.sparkArguments().containsKey("jars")) {

      String jarsConcatenated = sparkInterface.sparkArguments().get("jars");
      List<String> jarsList = Arrays.asList(jarsConcatenated.split(SPARK_ARGUMENT_DELIMITER));

      livyBatch.setJars(jarsList);

    }

    if (sparkInterface.sparkArguments().containsKey("pyFiles")) {

      String pyFilesConcatenated = sparkInterface.sparkArguments().get("pyFiles");
      List<String> pyFilesList = Arrays.asList(pyFilesConcatenated.split(SPARK_ARGUMENT_DELIMITER));

      livyBatch.setPyFiles(pyFilesList);
    }

    if (sparkInterface.sparkArguments().containsKey("files")) {

      String filesConcatenated = sparkInterface.sparkArguments().get("files");
      List<String> filesList = Arrays.asList(filesConcatenated.split(SPARK_ARGUMENT_DELIMITER));

      livyBatch.setPyFiles(filesList);

    }

    if (sparkInterface.sparkArguments().containsKey("driverMemory")) {
      livyBatch.setDriverMemory(sparkInterface.sparkArguments().get("driverMemory"));
    }

    if (sparkInterface.sparkArguments().containsKey("driverCores")
        && Integer.parseInt(sparkInterface.sparkArguments().get("driverCores")) > 0) {
      livyBatch
          .setDriverCores(Integer.parseInt(sparkInterface.sparkArguments().get("driverCores")));
    }

    if (sparkInterface.sparkArguments().containsKey("executorMemory")) {
      livyBatch.setExecutorMemory(sparkInterface.sparkArguments().get("executorMemory"));
    }

    if (sparkInterface.sparkArguments().containsKey("executorCores")
        && Integer.parseInt(sparkInterface.sparkArguments().get("executorCores")) > 0) {
      livyBatch
          .setExecutorCores(Integer.parseInt(sparkInterface.sparkArguments().get("executorCores")));
    }

    if (sparkInterface.sparkArguments().containsKey("numExecutors")
        && Integer.parseInt(sparkInterface.sparkArguments().get("numExecutors")) > 0) {
      livyBatch
          .setNumExecutors(Integer.parseInt(sparkInterface.sparkArguments().get("numExecutors")));
    }

    if (sparkInterface.sparkArguments().containsKey("archives")) {

      String archivesConcatenated = sparkInterface.sparkArguments().get("archives");
      List<String> archivesList = Arrays
          .asList(archivesConcatenated.split(SPARK_ARGUMENT_DELIMITER));

      livyBatch.setPyFiles(archivesList);
    }

    if (sparkInterface.sparkArguments().containsKey("queue")) {
      livyBatch.setQueue(sparkInterface.sparkArguments().get("queue"));
    }

    if (sparkInterface.sparkArguments().containsKey("name")) {
      livyBatch.setName(sparkInterface.sparkArguments().get("name"));
    }

    /**
     * add optional Spark configurations
     */
    livyBatch.setConf(sparkInterface.sparkConfiguration());

    return livyBatch;
  }


  public CloudiatorProcess execute(String userId, String schedule, Job job, Task task,
      NodeGroup nodeGroup) {

    LOGGER.info(String
        .format("Creating new CloudiatorProcess for user: %s, schedule %s, task %s on node %s",
            userId, schedule, task, nodeGroup));

    try {

      LOGGER.debug("Triggering Spark Worker installations...");
      this.installSparkWorkers(userId, nodeGroup);

      LOGGER.debug("Triggering Spark Process submission to Livy Server installations...");
      this.submitSparkProcessToLivy(task);

      //TODO: get Livy Batch ID from Livy Server as soon as this is fixed in Livy or YARN is enabled
      //using temporary UUID meanwhile
      UUID uuid = UUID.randomUUID();
      String temporarySparkProcessUid = uuid.toString();

      return CloudiatorClusterProcessBuilder.create().id(temporarySparkProcessUid)
          .userId(userId)
          .type(Type.SPARK)
          .state(ProcessState.CREATED)
          .nodeGroup(nodeGroup.id())
          .taskName(task.name()).scheduleId(schedule).build();

    } catch (Exception e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }

  }

}
