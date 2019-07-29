package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.cloudiator.deployment.domain.ProcessMapping;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OrderColumn;

@Entity
public class HdfsTaskInterfaceModel extends TaskInterfaceModel {

  @Column(nullable = false)
  private String file;

  @Nullable
  private String className;

  @OrderColumn
  @ElementCollection
  private List<String> arguments = new LinkedList<>();

  @ElementCollection
  private Map<String, String> hdfsArguments = new HashMap<>();

  @ElementCollection
  private Map<String, String> hdfsConfiguration = new HashMap<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProcessMapping processMapping;

  @Nullable
  public String getClassName() {
    return className;
  }

  public List<String> getArguments() {
    return ImmutableList.copyOf(arguments);
  }

  public HdfsTaskInterfaceModel addArgument(String argument) {
    checkNotNull(argument, "argument is null");
    arguments.add(argument);
    return this;
  }

  public Map<String, String> getHdfsArguments() {
    return ImmutableMap.copyOf(hdfsArguments);
  }

  public HdfsTaskInterfaceModel putHdfsArgument(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    hdfsArguments.put(key, value);
    return this;
  }

  public Map<String, String> getHdfsConfiguration() {
    return ImmutableMap.copyOf(hdfsConfiguration);
  }

  public HdfsTaskInterfaceModel putHdfsConfiguration(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    hdfsConfiguration.put(key, value);
    return this;
  }


  public String getFile() {
    return file;
  }

  public ProcessMapping getProcessMapping() {
    return processMapping;
  }

  /**
   * Empty hibernate constructor
   */
  protected HdfsTaskInterfaceModel() {
  }

  HdfsTaskInterfaceModel(TaskModel taskModel, String file, @Nullable String className,
      List<String> arguments, Map<String, String> hdfsArguments,
      Map<String, String> jdfsConfiguration, ProcessMapping processMapping) {
    super(taskModel);
    checkNotNull(file, "file is null");
    checkNotNull(arguments, "arguments is null");
    checkNotNull(hdfsArguments, "hdfsArguments is null");
    checkNotNull(hdfsConfiguration, "hdfsConfiguration is null");
    checkNotNull(processMapping, "processMapping is null");

    this.file = file;
    this.className = className;
    this.arguments = arguments;
    this.hdfsArguments = hdfsArguments;
    this.hdfsConfiguration = hdfsConfiguration;
    this.processMapping = processMapping;
  }

}
