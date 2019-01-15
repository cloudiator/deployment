package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OrderColumn;

@Entity
public class SparkTaskInterfaceModel extends TaskInterfaceModel {

  @Column(nullable = false)
  private String file;

  @Nullable
  private String className;

  @OrderColumn
  @ElementCollection
  private List<String> arguments = new LinkedList<>();

  @ElementCollection
  private Map<String, String> sparkArguments = new HashMap<>();

  @ElementCollection
  private Map<String, String> sparkConfiguration = new HashMap<>();

  @Nullable
  public String getClassName() {
    return className;
  }

  public List<String> getArguments() {
    return ImmutableList.copyOf(arguments);
  }

  public SparkTaskInterfaceModel addArgument(String argument) {
    checkNotNull(argument, "argument is null");
    arguments.add(argument);
    return this;
  }

  public Map<String, String> getSparkArguments() {
    return ImmutableMap.copyOf(sparkArguments);
  }

  public SparkTaskInterfaceModel putSparkArgument(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    sparkArguments.put(key, value);
    return this;
  }

  public Map<String, String> getSparkConfiguration() {
    return ImmutableMap.copyOf(sparkConfiguration);
  }

  public SparkTaskInterfaceModel putSparkConfiguration(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    sparkConfiguration.put(key, value);
    return this;
  }


  public String getFile() {
    return file;
  }

  /**
   * Empty hibernate constructor
   */
  protected SparkTaskInterfaceModel() {
  }

  SparkTaskInterfaceModel(TaskModel taskModel, String file, @Nullable String className,
      List<String> arguments, Map<String, String> sparkArguments,
      Map<String, String> sparkConfiguration) {
    super(taskModel);
    checkNotNull(file, "file is null");
    checkNotNull(arguments, "arguments is null");
    checkNotNull(sparkArguments, "sparkArguments is null");
    checkNotNull(sparkConfiguration, "sparkConfiguration is null");
    this.file = file;
    this.className = className;
    this.arguments = arguments;
    this.sparkArguments = sparkArguments;
    this.sparkConfiguration = sparkConfiguration;
  }

}
