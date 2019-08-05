package io.github.cloudiator.deployment.domain;

import java.util.Map;
import java.util.Set;

public interface FaasInterface extends TaskInterface {

  String functionName();

  String sourceCodeUrl();

  String handler();

  Set<Trigger> triggers();

  int timeout();

  Map<String, String> functionEnvironment();

}
