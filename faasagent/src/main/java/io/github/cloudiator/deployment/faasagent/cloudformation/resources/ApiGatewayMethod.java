package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import com.google.common.collect.ImmutableList;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ApiGatewayMethod extends AbstractResource {

  private final LambdaTemplate lambda;
  private final String transformedPath;

  public ApiGatewayMethod(LambdaTemplate lambda) {
    this.lambda = lambda;
    this.transformedPath = Arrays.stream(lambda.httpPath.split("/"))
        .map(StringUtils::capitalize)
        .collect(Collectors.joining());
  }

  @Override
  public String getName() {
    return String.join("",
        "ApiGatewayMethod",
        transformedPath,
        StringUtils.capitalize(lambda.httpMethod));
  }

  @Override
  protected String getType() {
    return "AWS::ApiGateway::Method";
  }

  @Override
  protected JSONObject getProperties() {
    return new JSONOrderedObject()
        .put("HttpMethod", lambda.httpMethod.toUpperCase())
        .put("RequestParameters", new JSONObject())
        .put("ResourceId", TemplateUtils.ref("ApiGatewayResource" + transformedPath))
        .put("RestApiId", TemplateUtils.ref(TemplateUtils.API_GATEWAY))
        .put("ApiKeyRequired", false)
        .put("AuthorizationType", "NONE")
        .put("Integration", getIntegration())
        .put("MethodResponses", new JSONArray());
  }

  private JSONObject getIntegration() {
    return new JSONOrderedObject()
        .put("IntegrationHttpMethod", "POST")
        .put("Type", "AWS_PROXY")
        .put("Uri", getLambdaUri());
  }

  private JSONObject getLambdaUri() {
    String lambdaId = lambda.name + "LambdaFunction";
    return TemplateUtils.join("", ImmutableList.of(
        "arn:",
        TemplateUtils.ref("AWS::Partition"),
        ":apigateway:",
        TemplateUtils.ref("AWS::Region"),
        ":lambda:path/2015-03-31/functions/",
        TemplateUtils.getAtt(lambdaId, "Arn"),
        "/invocations"));
  }
}
