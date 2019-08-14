package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.security.VariableContext;
import java.util.HashMap;
import java.util.Map;

public class DockerInterfaceBuilder {

  private String dockerImage;
  private Map<String, String> environment;
  private String portUpdateAction;

  private DockerInterfaceBuilder() {
    environment = new HashMap<>();
  }

  private DockerInterfaceBuilder(DockerInterface dockerInterface) {
    dockerImage = dockerInterface.dockerImage();
    environment = new HashMap<>(dockerInterface.environment());
    portUpdateAction = dockerInterface.portUpdateAction().orElse(null);
  }

  public static DockerInterfaceBuilder newBuilder() {
    return new DockerInterfaceBuilder();
  }

  public static DockerInterfaceBuilder of(DockerInterface dockerInterface) {
    return new DockerInterfaceBuilder(dockerInterface);
  }

  public DockerInterfaceBuilder dockerImage(String dockerImage) {
    this.dockerImage = dockerImage;
    return this;
  }

  public DockerInterfaceBuilder environment(Map<String, String> environment) {
    this.environment = environment;
    return this;
  }

  public DockerInterfaceBuilder putEnvVar(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    environment.put(key, value);
    return this;
  }

  public DockerInterfaceBuilder portUpdateAction(String portUpdateAction) {
    this.portUpdateAction = portUpdateAction;
    return this;
  }

  public DockerInterfaceBuilder decorate(VariableContext variableContext) {

    dockerImage = variableContext.parse(dockerImage);
    portUpdateAction = variableContext.parse(portUpdateAction);
    environment.replaceAll((k, v) -> variableContext.parse(v));

    return this;
  }

  public DockerInterface build() {
    return new DockerInterfaceImpl(dockerImage, environment, portUpdateAction);
  }
}
