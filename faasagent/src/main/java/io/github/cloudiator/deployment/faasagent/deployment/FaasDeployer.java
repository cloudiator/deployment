package io.github.cloudiator.deployment.faasagent.deployment;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;

import java.util.HashMap;
import java.util.Map;

public interface FaasDeployer {

  interface FaasDeployerFactory {

    boolean supports(Cloud cloud);

    FaasDeployer create(String region, Cloud cloud);

  }

  //return app id
  String deployApp(ApplicationTemplate app);

  //return map: functionName -> httpEndpoint
  Map<String, String> getApiEndpoints(ApplicationTemplate app);

  void removeApp(String stackName);

  default Map<String, String> getFunctionNames(ApplicationTemplate app) {
        Map<String, String> endpoints = new HashMap<>();
    for (LambdaTemplate model : app.functions) {
      endpoints.put(model.name, app.getFunctionFullName(model));
    }
    return endpoints;
  }

}
