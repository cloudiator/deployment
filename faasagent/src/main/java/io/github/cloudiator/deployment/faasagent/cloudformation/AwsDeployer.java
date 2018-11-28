package io.github.cloudiator.deployment.faasagent.cloudformation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
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
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer;
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

public class AwsDeployer implements FaasDeployer {

  public static class AwsDeployerFactory implements FaasDeployerFactory {

    @Override
    public boolean supports(Cloud cloud) {
      return "aws-ec2".equals(cloud.api().providerName());
    }

    @Override
    public FaasDeployer create(String region, Cloud cloud) {
      CloudCredential credential = cloud.credential();
      return new AwsDeployer(region, credential.user(), credential.password());
    }
  }

  private static final long WAIT_INTERVAL = 2000;
  private static final Logger LOGGER = LoggerFactory.getLogger(AwsDeployer.class);

  private final String region;
  private final AmazonCloudFormation cfApi;
  private final AmazonS3 amazonS3;
  private String physicalApiId;
  private String physicalBucketId;

  public AwsDeployer(String region, String keyId, String accessKey) {
    this.region = region;
    AWSCredentialsProvider credentials = getCredentials(keyId, accessKey);
    this.cfApi = AmazonCloudFormationClientBuilder.standard()
        .withCredentials(credentials)
        .withRegion(region)
        .build();
    this.amazonS3 = AmazonS3ClientBuilder.standard()
        .withCredentials(credentials)
        .withRegion(region)
        .build();
  }

  @Override
  public String deployApp(ApplicationTemplate app) {
    final CloudformationTemplateGenerator generator = new CloudformationTemplateGenerator(app);
    String stackName = app.name;

    // Create new Stack if doesn't exists
    boolean exists = cfApi.describeStacks(new DescribeStacksRequest()).getStacks()
        .stream().anyMatch((stack) -> stack.getStackName().equals(stackName));
    if (!exists) {
      try {
        createStack(stackName, generator.getCreateTemplate());
      } catch (InterruptedException e) {
        LOGGER.error("Creating stack interrupted", e);
        throw new RuntimeException("Failed to create stack");
      }
    } else {
      LOGGER.info("Stack {} exists, skipping creation", stackName);
    }

    // Get bucket ID
    physicalBucketId = getResourceId(stackName, TemplateUtils.BUCKET);

    // Transfer files from func source url to s3
    // remember s3 keys for each function
    for (LambdaTemplate template : app.functions) {
      try {
        template.s3CodeKey = template.name + ".zip";
        BufferedInputStream stream = new BufferedInputStream(new URL(template.codeUrl).openStream());
        LOGGER.info("Pushing file from {} to S3 bucket {}", template.codeUrl, physicalBucketId);
        amazonS3.putObject(physicalBucketId, template.s3CodeKey, stream, new ObjectMetadata());
      } catch (IOException e) {
        LOGGER.error("Moving file {} failed", template.codeUrl, e);
        throw new RuntimeException("Failed to prepare code sources");
      }
    }

    String updateTemplate = generator.getUpdateTemplate();
    LOGGER.debug(updateTemplate);
    try {
      updateStack(stackName, updateTemplate);
    } catch (InterruptedException e) {
      LOGGER.error("Updating stack interrupted", e);
      throw new RuntimeException("Failed to update stack");
    }
    physicalApiId = getResourceId(stackName, TemplateUtils.API_GATEWAY);

    LOGGER.info("Functions endpoints:");
    for (Map.Entry entry : getApiEndpoints(app).entrySet()) {
      LOGGER.info("{} -> {}", entry.getKey(), entry.getValue());
    }

    return physicalApiId;
  }

  @Override
  public Map<String, String> getApiEndpoints(ApplicationTemplate app) {
    physicalApiId = getResourceId(app.name, TemplateUtils.API_GATEWAY);
    if (physicalApiId == null) {
      throw new RuntimeException("Application must be deployed to get API endpoints");
    }
    Map<String, String> endpoints = new HashMap<>();
    for (LambdaTemplate model : app.functions) {
      endpoints.put(model.name, getFunctionEndpoint(model.httpPath));
    }
    return endpoints;
  }

  @Override
  public void removeApp(String stackName) {
    physicalBucketId = getResourceId(stackName, TemplateUtils.BUCKET);
    clearS3Bucket(physicalBucketId);
    String stackId = cfApi
        .describeStacks(new DescribeStacksRequest().withStackName(stackName))
        .getStacks().get(0).getStackId();
    try {
      deleteStack(stackId);
    } catch (InterruptedException e) {
      throw new RuntimeException("Failed to delete stack", e);
    }
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
        region, // 1
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
