package io.github.cloudiator.deployment.domain;

import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.domain.Runtime;

import java.util.UUID;

public class FunctionBuilder {

  private String id;
  private String cloudId;
  private String locationId;
  private int memory;
  private Runtime runtime;
  private String stackId;

  private FunctionBuilder() {
  }

  public static FunctionBuilder newBuilder() {
    return new FunctionBuilder();
  }

  public static FunctionBuilder newBuilder(Function function) {
    return new FunctionBuilder()
        .id(function.id())
        .cloudId(function.cloudId())
        .locationId(function.locationId())
        .memory(function.memory())
        .runtime(function.runtime())
        .stackId(function.stackId());
  }

  public FunctionBuilder generateId() {
    this.id = UUID.randomUUID().toString();
    return this;
  }

  public FunctionBuilder id(String id) {
    this.id = id;
    return this;
  }

  public FunctionBuilder cloudId(String cloudId) {
    this.cloudId = cloudId;
    return this;
  }

  public FunctionBuilder locationId(String locationId) {
    this.locationId = locationId;
    return this;
  }

  public FunctionBuilder memory(int memory) {
    this.memory = memory;
    return this;
  }

  public FunctionBuilder runtime(Runtime runtime) {
    this.runtime = runtime;
    return this;
  }

  public FunctionBuilder stackId(String stackId) {
    this.stackId = stackId;
    return this;
  }

  public Function build() {
    return new FunctionImpl(id, cloudId, locationId, memory, runtime, stackId);
  }
}
