package io.github.cloudiator.deployment.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface FaasInterface extends TaskInterface {

  String functionName();

  String sourceCodeUrl();

  String handler();

  String runtime();

  Set<Trigger> triggers();

  int timeout();

  int memory();

  Map<String, String> functionEnvironment();

}
