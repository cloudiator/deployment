package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONObject;

public class ApiGatewayRestApi extends AbstractResource {

  private final ApplicationTemplate app;

  public ApiGatewayRestApi(ApplicationTemplate app) {
    this.app = app;
  }

  @Override
  public String getName() {
    return TemplateUtils.API_GATEWAY;
  }

  @Override
  protected String getType() {
    return "AWS::ApiGateway::RestApi";
  }

  @Override
  protected JSONObject getProperties() {
    JSONObject props = new JSONObject();
    props.put("Name", app.name);
    return props;
  }
}
