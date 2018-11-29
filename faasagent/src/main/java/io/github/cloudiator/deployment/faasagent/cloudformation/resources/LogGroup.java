package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import org.json.JSONObject;

public class LogGroup extends AbstractResource {

  private final ApplicationTemplate app;
  private final LambdaTemplate lambda;

  public LogGroup(ApplicationTemplate app, LambdaTemplate lambda) {
    this.app = app;
    this.lambda = lambda;
  }

  @Override
  public String getName() {
    return lambda.name + "LogGroup";
  }

  @Override
  protected String getType() {
    return "AWS::Logs::LogGroup";
  }

  @Override
  protected JSONObject getProperties() {
    return new JSONObject()
        .put("LogGroupName",
            "/aws/lambda/" + app.getFunctionFullName(lambda));
  }
}
