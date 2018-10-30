package io.github.cloudiator.deployment.faasagent.cloudformation.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

//
public class TemplateUtils {

  public static final String BUCKET = "LambdaDeploymentBucket";
  public static final String API_GATEWAY = "ApiGatewayRestApi";
  public static final String ZIP_FILE = "apk.zip";
  public static final String IAM_ROLE = "IamRoleLambdaExecution";


  public static JSONObject singleton(String key, Object value) {
    JSONObject obj = new JSONObject();
    obj.put(key, value);
    return obj;
  }

  public static JSONObject ref(String ref) {
    return singleton("Ref", ref);
  }

  public static JSONObject sub(String sub) {
    return singleton("Fn::Sub", sub);
  }

  public static JSONObject join(String separator, List<Object> elements) {
    JSONArray join = new JSONArray();
    join.put(separator);
    JSONArray jsonElements = new JSONArray();
    for (Object elem : elements) {
      jsonElements.put(elem);
    }
    join.put(jsonElements);

    return singleton("Fn::Join", join);
  }

  public static JSONObject getAtt(String resource, String attr) {
    JSONArray params = new JSONArray();
    params.put(resource);
    params.put(attr);

    return singleton("Fn::GetAtt", params);
  }

}
