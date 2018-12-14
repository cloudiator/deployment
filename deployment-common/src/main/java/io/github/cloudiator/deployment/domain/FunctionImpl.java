package io.github.cloudiator.deployment.domain;

import io.github.cloudiator.domain.Runtime;

public class FunctionImpl implements Function {

  private final String id;
  private final String cloudId;
  private final String locationId;
  private final int memory;
  private final Runtime runtime;
  private final String stackId;

  FunctionImpl(String id, String cloudId, String locationId,
      int memory, Runtime runtime, String stackId) {
    this.id = id;
    this.cloudId = cloudId;
    this.locationId = locationId;
    this.memory = memory;
    this.runtime = runtime;
    this.stackId = stackId;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String cloudId() {
    return cloudId;
  }

  @Override
  public String locationId() {
    return locationId;
  }

  @Override
  public int memory() {
    return memory;
  }

  @Override
  public Runtime runtime() {
    return runtime;
  }

  @Override
  public String stackId() {
    return stackId;
  }
}
