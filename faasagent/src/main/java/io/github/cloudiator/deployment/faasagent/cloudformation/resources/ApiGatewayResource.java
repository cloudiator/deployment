package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONObject;

public class ApiGatewayResource extends AbstractResource {

  private JSONObject parent;
  private String pathPart;
  private String name;


  public ApiGatewayResource(JSONObject parent, String pathPart, String name) {
    this.parent = parent;
    this.pathPart = pathPart;
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  protected String getType() {
    return "AWS::ApiGateway::Resource";
  }

  @Override
  protected JSONObject getProperties() {
    JSONObject props = new JSONOrderedObject();
    props.put("ParentId", parent);
    props.put("PathPart", pathPart);
    props.put("RestApiId", TemplateUtils.ref(TemplateUtils.API_GATEWAY));
    return props;
  }
}
