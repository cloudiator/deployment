package io.github.cloudiator.deployment.spark;

import com.google.gson.Gson;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.deployment.config.Constants;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.SparkInterface;
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

/**
 * Created by Daniel Seybold on 06.11.2018.
 */
public class CreateSparkProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkProcessStrategy.class);

  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  private static final String SPARK_ARGUMENT_DELIMITER = ",";


  /**
   * Spark Default Settings
   */
  private static final int SPARK_DRIVER_CORES = 1;
  private static final String SPARK_DRIVER_MEMORY = "1G";
  private static final int SPARK_EXECUTOR_NUMBER = 1;
  private static final int SPARK_EXECUTOR_CORES = 1;
  private static final String SPARK_EXECUTOR_MEMORY = "1G";

  private static final String SPARK_PORT_RETRIES = "100";
  private static final String SPARK_DRIVER_PORT = "38000";
  private static final String SPARK_BLOCKMANAGER_PORT = "38100";
  private static final String SPARK_BROADCAST_PORT = "38200";
  private static final String SPARK_EXECUTOR_PORT = "38300";
  private static final String SPARK_FILESERVER_PORT = "38400";
  private static final String SPARK_REPLCASSSERVER_PORT = "38500";

  private InstallationRequestService installationRequestService;

  @Named(Constants.INSTALL_MELODIC_TOOLS)
  @Inject(optional = true)
  boolean installMelodicTools = false;

  @Inject
  CreateSparkProcessStrategy(InstallationRequestService installationRequestService) {
    this.installationRequestService = installationRequestService;
  }


  private void installSparkWorkers(String userId, Set<Node> nodes) {

    for (Node node : nodes) {

      //TODO: trigger sync install request and check if installation was successfull
      LOGGER.debug("Installing Docker and Spark Worker on node: " + node.id());

      final Builder builder = Installation.newBuilder()
          .setNode(NODE_MESSAGE_CONVERTER.apply(node))
          .addTool(Tool.DOCKER)
          .addTool(Tool.SPARK_WORKER);

      if(installMelodicTools){
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
            "Docker and Spark Worker  installation was interrupted during installation request.",
            e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Error during Docker and Spark Worker installation",
            e.getCause());
      }

      LOGGER.debug("Finished Docker and Spark Worker installation on node: " + node.id());
    }


  }


  private void submitSparkProcessToLivy(SparkInterface sparkInterface) {

    //find SparkInterface
    LOGGER.debug("Submitting Spark process to Livy Server...");

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
      LOGGER.debug("Submit Spark process to Livy Server payload: " + payload);

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
    } else {
      livyBatch.setDriverMemory(SPARK_DRIVER_MEMORY);
    }

    if (sparkInterface.sparkArguments().containsKey("driverCores")
        && Integer.parseInt(sparkInterface.sparkArguments().get("driverCores")) > 0) {
      livyBatch
          .setDriverCores(Integer.parseInt(sparkInterface.sparkArguments().get("driverCores")));
    } else {
      livyBatch.setDriverCores(SPARK_DRIVER_CORES);
    }

    if (sparkInterface.sparkArguments().containsKey("executorMemory")) {
      livyBatch.setExecutorMemory(sparkInterface.sparkArguments().get("executorMemory"));
    } else {
      livyBatch.setExecutorMemory(SPARK_EXECUTOR_MEMORY);
    }

    if (sparkInterface.sparkArguments().containsKey("executorCores")
        && Integer.parseInt(sparkInterface.sparkArguments().get("executorCores")) > 0) {
      livyBatch
          .setExecutorCores(Integer.parseInt(sparkInterface.sparkArguments().get("executorCores")));
    } else {
      livyBatch.setExecutorCores(SPARK_EXECUTOR_CORES);
    }

    if (sparkInterface.sparkArguments().containsKey("numExecutors")
        && Integer.parseInt(sparkInterface.sparkArguments().get("numExecutors")) > 0) {
      livyBatch
          .setNumExecutors(Integer.parseInt(sparkInterface.sparkArguments().get("numExecutors")));
    } else {
      livyBatch.setNumExecutors(SPARK_EXECUTOR_NUMBER);
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

    Map sparkDefaultConfMap = new HashMap<String, String>();

    /**
     * set default values in Spark configuration
     */
    if (!livyBatch.getConf().containsKey("spark.port.maxRetries")) {
      sparkDefaultConfMap.put("spark.port.maxRetries", SPARK_PORT_RETRIES);
    }

    if (!livyBatch.getConf().containsKey("spark.driver.port")) {
      sparkDefaultConfMap.put("spark.driver.port", SPARK_DRIVER_PORT);
    }

    if (!livyBatch.getConf().containsKey("spark.blockManager.port")) {
      sparkDefaultConfMap.put("spark.blockManager.port", SPARK_BLOCKMANAGER_PORT);
    }

    if (!livyBatch.getConf().containsKey("spark.broadcast.port")) {
      sparkDefaultConfMap.put("spark.broadcast.port", SPARK_BROADCAST_PORT);
    }

    if (!livyBatch.getConf().containsKey("spark.executor.port")) {
      sparkDefaultConfMap.put("spark.executor.port", SPARK_EXECUTOR_PORT);
    }

    if (!livyBatch.getConf().containsKey("spark.fileserver.port")) {
      sparkDefaultConfMap.put("spark.fileserver.port", SPARK_FILESERVER_PORT);
    }

    if (!livyBatch.getConf().containsKey("spark.replClassServer.port")) {
      sparkDefaultConfMap.put("spark.replClassServer.port", SPARK_REPLCASSSERVER_PORT);
    }

    Map sparkConfigMap = new HashMap<String, String>();
    sparkConfigMap.putAll(sparkDefaultConfMap);
    sparkConfigMap.putAll(sparkInterface.sparkConfiguration());

    livyBatch.setConf(sparkConfigMap);

    return livyBatch;
  }


  public CloudiatorProcess execute(String userId, String schedule, Task task,
      SparkInterface sparkInterface,
      Set<Node> nodes) {

    LOGGER.info(String
        .format("Creating new CloudiatorProcess for user: %s, schedule %s, task %s on nodes %s",
            userId, schedule, task, nodes));

    try {

      LOGGER.debug("Triggering Spark Worker installations...");
      this.installSparkWorkers(userId, nodes);

      LOGGER.debug("Triggering Spark Process submission to Livy Server installations...");
      this.submitSparkProcessToLivy(sparkInterface);

      //TODO: get Livy Batch ID from Livy Server as soon as this is fixed in Livy or YARN is enabled
      //using temporary UUID meanwhile
      UUID uuid = UUID.randomUUID();
      String temporarySparkProcessUid = uuid.toString();

      return CloudiatorClusterProcessBuilder.create().id(temporarySparkProcessUid)
          .originId(temporarySparkProcessUid)
          .userId(userId)
          .type(Type.SPARK)
          .taskInterface(SparkInterface.class.getCanonicalName())
          .state(ProcessState.RUNNING)
          .addAllNodes(nodes.stream().map(Identifiable::id).collect(Collectors.toList()))
          .taskName(task.name()).scheduleId(schedule).build();

    } catch (Exception e) {
      throw new IllegalStateException("Could not deploy task " + task, e);
    }

  }

}
