package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class LambdaFunction extends AbstractResource {

  private final ApplicationTemplate app;
  private final LambdaTemplate lambda;

  public LambdaFunction(ApplicationTemplate app, LambdaTemplate lambda) {
    this.app = app;
    this.lambda = lambda;
  }

  @Override
  public String getName() {
    return lambda.name + "LambdaFunction";
  }

  @Override
  protected String getType() {
    return "AWS::Lambda::Function";
  }

  @Override
  protected JSONObject getProperties() {
    return new JSONOrderedObject()
        .put("Code", new JSONObject()
            .put("S3Bucket", TemplateUtils.ref(TemplateUtils.BUCKET))
            .put("S3Key", lambda.s3CodeKey))
        .put("FunctionName", app.getFunctionFullName(lambda))
        .put("Handler", lambda.handler)
        .put("MemorySize", lambda.memorySize)
        .put("Runtime", lambda.runtime)
        .put("Timeout", lambda.timeout)
        .put("Environment", new JSONObject().put("Variables", lambda.env))
        .put("Role", TemplateUtils.getAtt(TemplateUtils.IAM_ROLE, "Arn"));
  }

  @Override
  protected JSONArray getDependencies() {
    return new JSONArray()
        .put(lambda.name + "LogGroup")
        .put(TemplateUtils.IAM_ROLE);
  }
}
