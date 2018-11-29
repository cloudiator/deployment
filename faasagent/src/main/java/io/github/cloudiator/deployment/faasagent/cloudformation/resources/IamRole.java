package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import com.google.common.base.Strings;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.JSONOrderedObject;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class IamRole extends AbstractResource {

  private ApplicationTemplate app;

  public IamRole(ApplicationTemplate app) {
    this.app = app;
  }

  @Override
  public String getName() {
    return TemplateUtils.IAM_ROLE;
  }

  @Override
  protected String getType() {
    return "AWS::IAM::Role";
  }

  @Override
  protected JSONObject getProperties() {
    return new JSONOrderedObject()
        .put("AssumeRolePolicyDocument", getAssumeRolePolicyDocument())
        .put("Policies", getPolicies())
        .put("Path", "/")
        .put("RoleName",
            String.join("-",
                app.name,
                app.region,
                "lambdaRole")
        );
  }

  private JSONObject getAssumeRolePolicyDocument() {
    return new JSONOrderedObject()
        .put("Version", "2012-10-17")
        .put("Statement", new JSONArray()
            .put(new JSONOrderedObject()
                .put("Effect", "Allow")
                .put("Principal", new JSONObject()
                    .put("Service", new JSONArray()
                        .put("lambda.amazonaws.com")))
                .put("Action", new JSONArray()
                    .put("sts:AssumeRole"))

            )
        );
  }

  private JSONArray getPolicies() {
    return new JSONArray()
        .put(new JSONOrderedObject()
            .put("PolicyName", String.join("-", app.name, "lambda"))
            .put("PolicyDocument", new JSONOrderedObject()
                .put("Version", "2012-10-17")
                .put("Statement", new JSONArray()
                    .put(getCreateLogStreamPolicyStatement())
                    .put(getPutLogEventsPolicyStatement()))
            )
        );
  }

  private JSONObject getCreateLogStreamPolicyStatement() {
    return new JSONOrderedObject()
        .put("Effect", "Allow")
        .put("Action", new JSONArray()
            .put("logs:CreateLogStream"))
        .put("Resource", getAllResources(1));
  }

  private JSONObject getPutLogEventsPolicyStatement() {
    return new JSONOrderedObject()
        .put("Effect", "Allow")
        .put("Action", new JSONArray()
            .put("logs:PutLogEvents"))
        .put("Resource", getAllResources(2));
  }

  private JSONArray getAllResources(int level) {
    JSONArray array = new JSONArray();
    for (LambdaTemplate lambda : app.functions) {
      array.put(getResource(lambda.name, level));
    }
    return array;
  }

  private JSONObject getResource(String lambdaName, int level) {
    String format = String.join(":",
        "arn",
        "${AWS::Partition}",
        "logs",
        "${AWS::Region}",
        "${AWS::AccountId}",
        "log-group",
        "/aws/lambda/%s-%s");
    return TemplateUtils.sub(String.format(format, app.name, lambdaName)
        + Strings.repeat(":*", level));
  }
}
