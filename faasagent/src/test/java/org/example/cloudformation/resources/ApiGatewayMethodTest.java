package org.example.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.resources.ApiGatewayMethod;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApiGatewayMethodTest {

  @Test
  public void getName() {
    ApiGatewayMethod method = new ApiGatewayMethod(ExampleModels.getLambdaModelOne());
    assertThat(method.getName(),
        is(equalTo("ApiGatewayMethodPathToFunctionGet")));
  }

  @Test
  public void getProperies() {
    ApiGatewayMethod method = new ApiGatewayMethod(ExampleModels.getLambdaModelOne());
    System.out.println(method.asJson().toString(2));
  }

}