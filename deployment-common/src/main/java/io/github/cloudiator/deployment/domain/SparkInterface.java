package io.github.cloudiator.deployment.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SparkInterface extends TaskInterface {

  String file();

  Optional<String> className();

  List<String> arguments();

  Map<String, String> sparkArguments();

  Map<String, String> sparkConfiguration();


}
