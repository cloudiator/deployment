package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

@Entity
public class SparkTaskInterfaceModel extends TaskInterfaceModel {

  @Column(nullable = false)
  private String file;

  @Nullable
  private String className;

  @ElementCollection
  private Set<String> arguments = new HashSet<>();

  @ElementCollection
  private Map<String, String> sparkArguments = new HashMap<>();

  @ElementCollection
  private Map<String, String> sparkConfiguration = new HashMap<>();

  @Nullable
  public String getClassName() {
    return className;
  }

  public Set<String> getArguments() {
    return ImmutableSet.copyOf(arguments);
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
      Set<String> arguments, Map<String, String> sparkArguments,
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
