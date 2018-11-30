package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class FunctionModel extends Model {

  @Column(nullable = false, updatable = false)
  private String functionId;

  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  @Column(nullable = false, updatable = false)
  private String cloudId;

  @Column(nullable = false, updatable = false)
  private String locationId;

  @Column
  private Integer memory;

  @Column
  private String runtime;

  @Column
  private String stackId;

  protected FunctionModel() {
  }

  public FunctionModel(String functionId, TenantModel tenantModel, String cloudId,
      String locationId, Integer memory, String runtime, String stackId) {
    this.functionId = functionId;
    this.tenantModel = tenantModel;
    this.cloudId = cloudId;
    this.locationId = locationId;
    this.memory = memory;
    this.runtime = runtime;
    this.stackId = stackId;
  }

  public String getFunctionId() {
    return functionId;
  }

  public TenantModel getTenantModel() {
    return tenantModel;
  }

  public String getCloudId() {
    return cloudId;
  }

  public String getLocationId() {
    return locationId;
  }

  public Integer getMemory() {
    return memory;
  }

  public String getRuntime() {
    return runtime;
  }

  public String getStackId() {
    return stackId;
  }

  public FunctionModel setStackId(String stackId) {
    this.stackId = stackId;
    return this;
  }
}
