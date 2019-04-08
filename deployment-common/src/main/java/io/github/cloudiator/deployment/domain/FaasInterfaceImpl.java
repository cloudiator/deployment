package io.github.cloudiator.deployment.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class FaasInterfaceImpl implements FaasInterface {

  private final String functionName;
  private final String sourceCodeUrl;
  private final String handler;
  private final Set<Trigger> triggers;
  private final int timeout;
  private Map<String, String> functionEnvironment;


  FaasInterfaceImpl(
      String functionName,
      String sourceCodeUrl,
      String handler,
      Set<Trigger> triggers,
      int timeout,
      Map<String, String> functionEnvironment
  ) {
    checkNotNull(functionName, "functionName is null");
    checkNotNull(sourceCodeUrl, "sourceCodeUrl is null");
    checkNotNull(handler, "handler is null");
    checkNotNull(triggers, "triggers is null");
    checkNotNull(functionEnvironment, "Function environment is null");
    this.functionName = functionName;
    this.sourceCodeUrl = sourceCodeUrl;
    this.handler = handler;
    this.triggers = triggers;
    this.timeout = timeout;
    this.functionEnvironment = functionEnvironment;
  }

  @Override
  public String functionName() {
    return functionName;
  }

  @Override
  public String sourceCodeUrl() {
    return sourceCodeUrl;
  }

  @Override
  public String handler() {
    return handler;
  }

  @Override
  public Set<Trigger> triggers() {
    return triggers;
  }

  @Override
  public int timeout() {
    return timeout;
  }

  @Override
  public Map<String, String> functionEnvironment() {
    return functionEnvironment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FaasInterfaceImpl that = (FaasInterfaceImpl) o;
    return
        Objects.equals(functionName, that.functionName) &&
        Objects.equals(sourceCodeUrl, that.sourceCodeUrl) &&
        Objects.equals(handler, that.handler) &&
        Objects.equals(triggers, that.triggers) &&
        Objects.equals(timeout, that.timeout) &&
        Objects.equals(functionEnvironment, that.functionEnvironment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(functionName, sourceCodeUrl,
        handler, triggers, timeout, functionEnvironment);
  }

  @Override
  public String toString() {
    return "FaasInterfaceImpl{" +
        "functionName='" + functionName + '\'' +
        ", sourceCodeUrl='" + sourceCodeUrl + '\'' +
        ", handler='" + handler + '\'' +
        ", triggers=" + triggers +
        ", timeout=" + timeout +
        ", functionEnvironment=" + functionEnvironment +
        '}';
  }

  @Override
  public ProcessMapping processMapping() {
    return ProcessMapping.SINGLE;
  }
}
