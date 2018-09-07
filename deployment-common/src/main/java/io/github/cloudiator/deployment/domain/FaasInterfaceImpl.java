package io.github.cloudiator.deployment.domain;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class FaasInterfaceImpl implements FaasInterface {

  private final String functionName;
  private final String sourceCodeUrl;
  private final String handler;
  private final String runtime;
  private final Set<Trigger> triggers;
  private final int timeout;
  private final int memory;

  FaasInterfaceImpl(
      String functionName,
      String sourceCodeUrl,
      String handler,
      String runtime,
      Set<Trigger> triggers,
      int timeout,
      int memory
  ) {
    checkNotNull(functionName, "functionName is null");
    checkNotNull(sourceCodeUrl, "sourceCodeUrl is null");
    checkNotNull(handler, "handler is null");
    checkNotNull(runtime, "runtime is null");
    checkNotNull(triggers, "triggers is null");
    this.functionName = functionName;
    this.sourceCodeUrl = sourceCodeUrl;
    this.handler = handler;
    this.runtime = runtime;
    this.triggers = triggers;
    this.timeout = timeout;
    this.memory = memory;
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
  public String runtime() {
    return runtime;
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
  public int memory() {
    return memory;
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
        Objects.equals(runtime, that.runtime) &&
        Objects.equals(triggers, that.triggers) &&
        Objects.equals(timeout, that.timeout) &&
        Objects.equals(memory, that.memory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(functionName, sourceCodeUrl,
        handler, runtime, triggers, timeout, memory);
  }

  @Override
  public String toString() {
    return "FaasInterfaceImpl{" +
        "functionName='" + functionName + '\'' +
        ", sourceCodeUrl='" + sourceCodeUrl + '\'' +
        ", handler='" + handler + '\'' +
        ", runtime='" + runtime + '\'' +
        ", triggers=" + triggers +
        ", timeout=" + timeout +
        ", memory='" + memory +
        '}';
  }
}
