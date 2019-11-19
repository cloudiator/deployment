package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.deployment.security.VariableContext;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    return MoreObjects.toStringHelper(this)
        .add("functionName", functionName)
        .add("sourceCodeUrl", sourceCodeUrl)
        .add("handler", handler)
        .add("triggers", triggers)
        .add("timeout", timeout)
        .add("functionEnvironment", functionEnvironment)
        .toString();
  }

  @Override
  public ProcessMapping processMapping() {
    return ProcessMapping.SINGLE;
  }

  @Override
  public boolean isStaticallyConfigured() {
    return true;
  }

  @Override
  public boolean requiresManualWait(TaskInterface dependency) {
    return true;
  }

  @Override
  public TaskInterface decorateEnvironment(Environment environment) {
    return this;
  }

  @Override
  public TaskInterface decorateVariables(VariableContext variableContext) {
    return this;
  }
}
