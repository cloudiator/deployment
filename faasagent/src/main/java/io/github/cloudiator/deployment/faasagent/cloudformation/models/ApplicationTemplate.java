package io.github.cloudiator.deployment.faasagent.cloudformation.models;

import java.util.List;
import java.util.Optional;

public class ApplicationTemplate {
  public String name; //Task name
  public String region; // based on node
  public List<LambdaTemplate> functions; // From task interfaces

  public String getFunctionFullName(LambdaTemplate fun) {
    return String.format("%s-%s", name, fun.name);
  }

  public Optional<LambdaTemplate> getFunctionByName(String functionName) {
    if (functions == null) {
      return Optional.empty();
    }
    return functions.stream()
            .filter(lambdaTemplate -> lambdaTemplate.name.equals(functionName))
            .findFirst();
  }

}
