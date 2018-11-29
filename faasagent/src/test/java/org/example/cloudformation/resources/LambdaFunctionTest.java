package org.example.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.resources.LambdaFunction;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LambdaFunctionTest {

  @Test
  public void testAsJsonElement() {
    LambdaFunction lambda = new LambdaFunction(
        ExampleModels.getAppModelTwo(),
        ExampleModels.getLambdaModelOne());
    JSONObject obj = lambda.asJson();
    assertThat(obj.length(), is(equalTo(3)));
    System.out.println(obj.toString(2));
  }

  @Test
  public void testJsonWriter() {
    LambdaTemplate model = ExampleModels.getLambdaModelOne();
    System.out.println(model.hashCode());
    System.out.println(JSONWriter.valueToString(model));
  }
}