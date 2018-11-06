package io.github.cloudiator.deployment.spark;

import com.google.gson.Gson;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcessBuilder;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.domain.Node;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 06.11.2018.
 */
public class CreateSparkProcessStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSparkProcessStrategy.class);

  @Inject
  CreateSparkProcessStrategy() {
   //TODO: add additional services if required
  }


  private void installSparkWorkers(List<Node> nodes){

    for (Node node: nodes) {

      //TODO: trigger sync install request and check if installation was successfull

    }

  }


  private void submitSparkProcessToLivy(Task task){


    //find SparkInterface
    SparkInterface sparkInterface = null;
    for (TaskInterface taskInterface: task.interfaces()) {

      if(taskInterface.getClass().equals(SparkInterface.class)){
        sparkInterface = (SparkInterface)taskInterface;
        LOGGER.debug("Found a SparkInterface in TaskInterfaces, continueing with Spark process submission! Multiple Spark Interfaces in one Task are currently not supported!");
        break;
      }
    }
    if(sparkInterface == null) throw  new IllegalStateException("No SparkInterface in TaskInterface set was found! Aborting Spark Process submission!");


    //execute HTTP POST call to Livy Server
    ResponseHandler<String> handler = new BasicResponseHandler();


    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("http://134.60.64.164:8998/batches");

    LivyBatch livyBatch =buildLivyBatch(sparkInterface);

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

      if(code != HttpStatus.SC_OK){
        throw new IllegalStateException("Submission of Spark process to livy faild with response code: " + code);
      }

      //TODO: get appId from response (as soon as Livy bug is fixed and appId is properly set)
      String body = handler.handleResponse(response);
      client.close();


    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Error while creating HTTP Post payload for Livy Server call!",e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    } catch (ClientProtocolException e) {
      LOGGER.error("Error while executing HTTP Post call to Livy Server!",e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    } catch (IOException e) {
      LOGGER.error("Error while handling HTTP Post response from Livy Server!",e);
      throw new IllegalStateException("Error while submitting Spark Process to Livy Server!");
    }


  }

  private static LivyBatch buildLivyBatch(SparkInterface sparkInterface){

    //TODO: build Livy Batch
    return null;
  }



  public CloudiatorProcess execute(String userId, String schedule, Job job, Task task, Node node) {



    LOGGER.info(String
        .format("Creating new CloudiatorProcess for user: %s, schedule %s, task %s on node %s",
            userId, schedule, task, node));

    try{
    //TODO: trigger the installation of the spark worker over all nodes

    LOGGER.debug("Triggering Spark Worker installations...");
    //TODO refactor as sonn as a list of nodes is available
    List<Node> nodesToPrepareSpark = new ArrayList<>();
    nodesToPrepareSpark.add(node);

    this.installSparkWorkers(nodesToPrepareSpark);

    //TODO: extract the spark process attributes and submit the process to livy server
    LOGGER.debug("Triggering Spark Process submission to Livy Server installations...");

    this.submitSparkProcessToLivy(task);

    return CloudiatorProcessBuilder.newBuilder().id("spark-dummy-id")
        .nodeId(node.id())
        .taskName(task.name()).scheduleId(schedule).build();

    } catch (Exception e) {
        throw new IllegalStateException("Could not deploy task " + task, e);
    }

  }

}
