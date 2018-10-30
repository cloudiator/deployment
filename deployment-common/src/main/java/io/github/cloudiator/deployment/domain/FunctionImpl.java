package io.github.cloudiator.deployment.domain;

import de.uniulm.omi.cloudiator.sword.domain.Location;

public class FunctionImpl implements Function {

  private final String id;
  private final String cloudId;
  private final String locationId;
  private final int memory;

  FunctionImpl(String id, String cloudId, String locationId, int memory) {
    this.id = id;
    this.cloudId = cloudId;
    this.locationId = locationId;
    this.memory = memory;
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
}
