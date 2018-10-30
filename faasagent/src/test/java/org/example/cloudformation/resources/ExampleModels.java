package org.example.cloudformation.resources;

import com.amazonaws.regions.Regions;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;

import java.util.HashMap;
import java.util.LinkedList;

public class ExampleModels {

  static LambdaTemplate getLambdaModelOne() {
    LambdaTemplate lambda = new LambdaTemplate();
    lambda.name = "lambdaName";
    lambda.codeUrl = "www.example.com/lambdaOne.zip";
    lambda.handler = "file.function";
    lambda.httpMethod = "get";
    lambda.httpPath = "path/to/function";
    lambda.memorySize = 2048;
    lambda.timeout = 10;
    lambda.runtime = "node6.10";
    lambda.env = new HashMap<>();
    lambda.env.put("Key1", "Value1");
    return lambda;
  }

  private static LambdaTemplate getLambdaModelTwo() {
    LambdaTemplate lambda = new LambdaTemplate();
    lambda.name = "secondLambda";
    lambda.codeUrl = "www.example.com/lambdaTwo.zip";
    lambda.handler = "code.lambda";
    lambda.httpMethod = "post";
    lambda.httpPath = "api/gateway/func";
    lambda.memorySize = 1024;
    lambda.timeout = 22;
    lambda.runtime = "nodejs6.10";
    lambda.env = new HashMap<>();
    lambda.env.put("Key2", "Value2");
    return lambda;
  }

  static ApplicationTemplate getAppModelZero() {
    ApplicationTemplate app = new ApplicationTemplate();
    app.name = "TestApp";
    app.region = Regions.EU_WEST_1;
    app.functions = new LinkedList<>();
    return app;
  }

  static ApplicationTemplate getAppModelOne() {
    ApplicationTemplate app = getAppModelZero();
    app.functions.add(getLambdaModelOne());
    return app;
  }


  public static ApplicationTemplate getAppModelTwo() {
    ApplicationTemplate app = getAppModelOne();
    app.functions.add(getLambdaModelTwo());
    return app;
  }
}
