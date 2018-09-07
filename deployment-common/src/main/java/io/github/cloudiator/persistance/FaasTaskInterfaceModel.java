package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
public class FaasTaskInterfaceModel extends TaskInterfaceModel {

  @Column(nullable = false)
  private String functionName;
  @Column(nullable = false)
  private String sourceCodeUrl;
  @Column(nullable = false)
  private String handler;
  @Column(nullable = false)
  private String runtime;
  @OneToMany(mappedBy = "faasInterface")
  private Set<TriggerModel> triggers;
  @Column(nullable = false)
  private int timeout;
  @Column(nullable = false)
  private int memory;

  protected FaasTaskInterfaceModel() {
  }

  public FaasTaskInterfaceModel(TaskModel task, String functionName,
      String sourceCodeUrl, String handler,
      String runtime, int timeout, int memory) {
    super(task);

    checkNotNull(functionName, "functionName is null");
    checkNotNull(sourceCodeUrl, "sourceCodeUrl is null");
    checkNotNull(handler, "handler is null");
    checkNotNull(runtime, "runtime is null");

    this.functionName = functionName;
    this.sourceCodeUrl = sourceCodeUrl;
    this.handler = handler;
    this.runtime = runtime;
    this.timeout = timeout;
    this.memory = memory;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getSourceCodeUrl() {
    return sourceCodeUrl;
  }

  public void setSourceCodeUrl(String sourceCodeUrl) {
    this.sourceCodeUrl = sourceCodeUrl;
  }

  public String getHandler() {
    return handler;
  }

  public void setHandler(String handler) {
    this.handler = handler;
  }

  public String getRuntime() {
    return runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }

  public Set<TriggerModel> getTriggers() {
    return triggers;
  }

  public void setTriggers(Set<TriggerModel> triggers) {
    this.triggers = triggers;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getMemory() {
    return memory;
  }

  public void setMemory(int memory) {
    this.memory = memory;
  }
}
