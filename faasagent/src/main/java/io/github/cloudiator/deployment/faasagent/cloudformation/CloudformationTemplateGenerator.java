package io.github.cloudiator.deployment.faasagent.cloudformation;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.resources.*;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import org.json.JSONObject;

public class CloudformationTemplateGenerator {

  private final ApplicationTemplate app;

  public CloudformationTemplateGenerator(ApplicationTemplate app) {
    this.app = app;
  }

  public String getCreateTemplate() {
    S3Bucket bucket = new S3Bucket();
    JSONObject template = new JSONOrderedObject()
        .put("AWSTemplateFormatVersion", "2010-09-09")
        .put("Resources", new JSONOrderedObject()
            .put(bucket.getName(), bucket.asJson()));
    return template.toString(2);
  }

  public String getUpdateTemplate() {
    JSONObject template = new JSONOrderedObject()
        .put("AWSTemplateFormatVersion", "2010-09-09")
        .put("Resources", getResources());
    return template.toString(2);
  }

  private JSONObject getResources() {
    JSONObject res = new JSONOrderedObject();
    putResource(res, new S3Bucket());
    putResource(res, new IamRole(app));
    for (LambdaTemplate lambda : app.functions) {
      putResource(res, new LogGroup(app, lambda));
      putResource(res, new LambdaFunction(app, lambda));
      putResource(res, new ApiGatewayMethod(lambda));
      putResource(res, new LambdaPermission(lambda));
    }
    putResource(res, new ApiGatewayRestApi(app));
    ApiPathTree pathTree = new ApiPathTree(app.functions);
    for (AbstractResource resource : pathTree.getResources()) {
      putResource(res, resource);
    }
    putResource(res, new ApiGatewayDeployment(app));
    return res;
  }

  private void putResource(JSONObject into, AbstractResource resource) {
    into.put(resource.getName(), resource.asJson());
  }
}
