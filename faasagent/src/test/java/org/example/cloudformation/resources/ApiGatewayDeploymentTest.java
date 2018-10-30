package org.example.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.resources.ApiGatewayDeployment;
import org.junit.Test;

public class ApiGatewayDeploymentTest {

  @Test
  public void asJson() {
    ApiGatewayDeployment deployment =
        new ApiGatewayDeployment(ExampleModels.getAppModelTwo());
    System.out.println(deployment.asJson());
  }
}