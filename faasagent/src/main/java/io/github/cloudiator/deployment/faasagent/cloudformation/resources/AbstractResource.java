package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import org.json.JSONArray;
import org.json.JSONObject;

// TODO Outputs??

public abstract class AbstractResource {

  public JSONObject asJson() {
    JSONObject ret = new JSONOrderedObject();
    ret.put("Type", getType());
    JSONObject properties = getProperties();
    if (properties != null) {
      ret.put("Properties", properties);
    }
    JSONArray dependsOn = getDependencies();
    if (dependsOn != null) {
      ret.put("DependsOn", dependsOn);
    }
    return ret;
  }

  abstract public String getName();

  abstract protected String getType();

  protected JSONObject getProperties() {
    return null;
  }

  protected JSONArray getDependencies() {
    return null;
  }
}
