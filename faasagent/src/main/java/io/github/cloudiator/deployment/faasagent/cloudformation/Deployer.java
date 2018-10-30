package io.github.cloudiator.deployment.faasagent.cloudformation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO create deployer interface
public class Deployer {
  private static final long WAIT_INTERVAL = 2000;
  private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);

  private final ApplicationTemplate app;
  private final AmazonCloudFormation cfApi;
  private final AmazonS3 amazonS3;
  private final String stackName;
  private String physicalApiId;
  private String physicalBucketId;

  public Deployer(ApplicationTemplate app, String keyId, String accessKey) {
    this.app = app;
    AWSCredentialsProvider credentials = getCredentials(keyId, accessKey);
    this.cfApi = AmazonCloudFormationClientBuilder.standard()
        .withCredentials(credentials)
        .withRegion(app.region)
        .build();
    this.amazonS3 = AmazonS3ClientBuilder.standard()
        .withCredentials(credentials)
        .withRegion(app.region)
        .build();
    this.stackName = app.name;
  }

  public String deployApp() throws InterruptedException, IOException {
    final CloudformationTemplateGenerator generator = new CloudformationTemplateGenerator(app);

    // Create new Stack if doesn't exists
    boolean exists = cfApi.describeStacks(new DescribeStacksRequest()).getStacks()
        .stream().anyMatch((stack) -> stack.getStackName().equals(stackName));
    if (!exists) {
      createStack(stackName, generator.getCreateTemplate());
    } else {
      LOGGER.info("Stack {} exists, skipping creation", stackName);
    }

    // Get bucket ID
    physicalBucketId = getResourceId(stackName, TemplateUtils.BUCKET);

    // Transfer files from func source url to s3
    // remember s3 keys for each function
    for (LambdaTemplate template : app.functions) {
      template.s3CodeKey = template.name + ".zip";
      BufferedInputStream stream = new BufferedInputStream(new URL(template.codeUrl).openStream());
      LOGGER.info("Pushing file from {} to S3 bucket {}", template.codeUrl, physicalBucketId);
      amazonS3.putObject(physicalBucketId, template.s3CodeKey, stream, new ObjectMetadata());
    }

    String updateTemplate = generator.getUpdateTemplate();
    LOGGER.debug(updateTemplate);
    updateStack(stackName, updateTemplate);

    physicalApiId = getResourceId(stackName, TemplateUtils.API_GATEWAY);

    LOGGER.info("Functions endpoints:");
    for (Map.Entry entry : getApiEndpoints().entrySet()) {
      LOGGER.info("{} -> {}", entry.getKey(), entry.getValue());
    }

    return physicalApiId;
  }

  public Map<String, String> getApiEndpoints() throws RuntimeException {
    if (physicalApiId == null) {
      throw new RuntimeException("Application must be deployed to get API endpoints");
    }
    Map<String, String> endpoints = new HashMap<>();
    for (LambdaTemplate model : app.functions) {
      endpoints.put(model.name, getFunctionEndpoint(model.httpPath));
    }
    return endpoints;
  }

  public void removeApp() throws InterruptedException {
    physicalBucketId = getResourceId(stackName, TemplateUtils.BUCKET);
    clearS3Bucket(physicalBucketId);
    String stackId = cfApi
        .describeStacks(new DescribeStacksRequest().withStackName(stackName))
        .getStacks().get(0).getStackId();
    deleteStack(stackId);
  }

  private String getResourceId(String stackName, String resourceLogicalId) {
    DescribeStackResourceRequest logicalNameResourceRequest =
        new DescribeStackResourceRequest()
            .withStackName(stackName)
            .withLogicalResourceId(resourceLogicalId);
    StackResourceDetail resourceDetail =
        cfApi.describeStackResource(logicalNameResourceRequest).getStackResourceDetail();
    return resourceDetail.getPhysicalResourceId();
  }

  private String getFunctionEndpoint(String functionPath) {
    return MessageFormat.format(
        "https://{0}.execute-api.{1}.amazonaws.com/prod/{2}",
        physicalApiId, // 0
        app.region.getName(), // 1
        functionPath); // 2
  }

  private void createStack(String stackName, String template) throws InterruptedException {
    CreateStackRequest createRequest = new CreateStackRequest();
    createRequest.setStackName(stackName);
    createRequest.setTemplateBody(template);
    LOGGER.info("Creating a stack named {}", createRequest.getStackName());
    cfApi.createStack(createRequest);
    String status = waitForCompletion(stackName);
    LOGGER.info("{} stack creation completed, with status {}", stackName, status);
  }

  private void updateStack(String stackName, String template) throws InterruptedException {
    UpdateStackRequest updateRequest = new UpdateStackRequest();
    updateRequest.setStackName(stackName);
    updateRequest.setTemplateBody(template);
    updateRequest.setCapabilities(
        ImmutableList.of(Capability.CAPABILITY_NAMED_IAM.toString()));
    LOGGER.info("Updating a stack named {}", updateRequest.getStackName());
    cfApi.updateStack(updateRequest);

    String status = waitForCompletion(stackName);
    LOGGER.info("{} stack update completed, with status {}", stackName, status);
  }

  private void clearS3Bucket(String bucketId) {
    ObjectListing objectListing = amazonS3.listObjects(bucketId);
    while (true) {
      for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
        amazonS3.deleteObject(bucketId, s3ObjectSummary.getKey());
      }
      if (objectListing.isTruncated()) {
        objectListing = amazonS3.listNextBatchOfObjects(objectListing);
      } else {
        break;
      }
    }
  }

  private void deleteStack(String stackId) throws InterruptedException {
    DeleteStackRequest deleteRequest = new DeleteStackRequest();
    deleteRequest.setStackName(stackId);
    LOGGER.info("Deleting the stack with ID {}", deleteRequest.getStackName());
    cfApi.deleteStack(deleteRequest);

    String status = waitForCompletion(stackId);
    LOGGER.info("Stack {} deletion completed, with status {}", stackId, status);
  }

  private AWSCredentialsProvider getCredentials(String keyId, String accessKey) throws AmazonClientException {
    return new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(keyId, accessKey)
    );
  }


  private String waitForCompletion(String stackName) throws InterruptedException {
    DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest()
        .withStackName(stackName);
    Boolean completed = false;
    String stackStatus = "";
    String stackReason = "";

    final Set<String> endStates = ImmutableSet.of(
        StackStatus.CREATE_COMPLETE.toString(),
        StackStatus.UPDATE_COMPLETE.toString(),
        StackStatus.UPDATE_ROLLBACK_COMPLETE.toString(), // Means update failed
        StackStatus.DELETE_COMPLETE.toString(),
        StackStatus.CREATE_FAILED.toString(),
        StackStatus.ROLLBACK_FAILED.toString(),
        StackStatus.DELETE_FAILED.toString()
    );

    while (!completed) {
      // It should return only one stack
      List<Stack> stacks = cfApi.describeStacks(describeStacksRequest).getStacks();
      for (Stack stack : stacks) {
        if (endStates.contains(stack.getStackStatus())) {
          completed = true;
          stackStatus = stack.getStackStatus();
          stackReason = stack.getStackStatusReason();
        } else {
          Thread.sleep(WAIT_INTERVAL);
        }
      }
    }

    return stackStatus + " (" + stackReason + ")";
  }
}
