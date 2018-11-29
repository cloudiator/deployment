package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiGatewayDeployment extends AbstractResource {

  private final ApplicationTemplate app;

  public ApiGatewayDeployment(ApplicationTemplate app) {
    this.app = app;
  }

  @Override
  public String getName() {
    return "ApiGatewayDeployment" + System.currentTimeMillis();
  }

  @Override
  protected String getType() {
    return "AWS::ApiGateway::Deployment";
  }

  @Override
  protected JSONObject getProperties() {
    return new JSONOrderedObject()
        .put("RestApiId", TemplateUtils.ref(TemplateUtils.API_GATEWAY))
        .put("StageName", "prod");
  }

  @Override
  protected JSONArray getDependencies() {
    JSONArray array = new JSONArray();
    for (LambdaTemplate lambda : app.functions) {
      array.put(new ApiGatewayMethod(lambda).getName());
    }
    return array;
  }
}
