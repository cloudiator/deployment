package io.github.cloudiator.deployment.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FaasInterfaceBuilder {

  private String functionName;
  private String sourceCodeUrl;
  private String handler;
  private Set<Trigger> triggers;
  private int timeout;
  private Map<String, String> functionEnvironment;

  private FaasInterfaceBuilder() {
  }

  public static FaasInterfaceBuilder newBuilder() {
    return new FaasInterfaceBuilder();
  }

  public FaasInterfaceBuilder functionName(String functionName) {
    this.functionName = functionName;
    return this;
  }

  public FaasInterfaceBuilder sourceCodeUrl(String sourceCodeUrl) {
    this.sourceCodeUrl = sourceCodeUrl;
    return this;
  }

  public FaasInterfaceBuilder handler(String handler) {
    this.handler = handler;
    return this;
  }

  public FaasInterfaceBuilder triggers(Set<Trigger> triggers) {
    this.triggers = triggers;
    return this;
  }

  public FaasInterfaceBuilder timeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public FaasInterfaceBuilder functionEnvironment(Map<String, String> functionEnvironment) {
    this.functionEnvironment = functionEnvironment;
    return this;
  }

  public FaasInterface build() {
    return new FaasInterfaceImpl(
        functionName,
        sourceCodeUrl,
        handler,
        triggers,
        timeout,
        functionEnvironment
    );
  }

}
