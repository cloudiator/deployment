package io.github.cloudiator.deployment.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HdfsInterface extends TaskInterface {

  String file();

  Optional<String> className();

  List<String> arguments();

  Map<String, String> hdfsArguments();

  Map<String, String> hdfsConfiguration();


}
