package io.github.cloudiator.deployment.domain;

public class FunctionImpl implements Function {

  private final String id;
  private final String cloudId;
  private final String locationId;
  private final int memory;
  private final String stackId;

  FunctionImpl(String id, String cloudId, String locationId, int memory, String stackId) {
    this.id = id;
    this.cloudId = cloudId;
    this.locationId = locationId;
    this.memory = memory;
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
  public String stackId() {
    return stackId;
  }
}
