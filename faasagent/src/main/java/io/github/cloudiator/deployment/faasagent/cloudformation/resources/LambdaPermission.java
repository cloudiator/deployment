package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class LambdaPermission extends AbstractResource {

  private final LambdaTemplate lambda;

  public LambdaPermission(LambdaTemplate lambda) {
    this.lambda = lambda;
  }

  @Override
  public String getName() {
    return lambda.name + "LambdaPermissionApiGateway";
  }

  @Override
  protected String getType() {
    return "AWS::Lambda::Permission";
  }

  @Override
  protected JSONObject getProperties() {
    final String sourceArn = String.join(":",
        "arn",
        "${AWS::Partition}",
        "execute-api",
        "${AWS::Region}",
        "${AWS::AccountId}",
        "${ApiGatewayRestApi}/*/*");
    LambdaFunction fun = new LambdaFunction(null, lambda);
    return new JSONOrderedObject()
        .put("FunctionName", TemplateUtils.getAtt(fun.getName(), "Arn"))
        .put("Action", "lambda:InvokeFunction")
        .put("Principal", TemplateUtils.sub("apigateway.${AWS::URLSuffix}"))
        .put("SourceArn", TemplateUtils.sub(sourceArn));
  }

  @Override
  protected JSONArray getDependencies() {
    return super.getDependencies();
  }
}
