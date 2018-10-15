package io.github.cloudiator.deployment.domain;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SparkInterface extends TaskInterface {

  String file();

  Optional<String> className();

  Set<String> arguments();

  Map<String, String> sparkArguments();

  Map<String, String> sparkConfiguration();


}
