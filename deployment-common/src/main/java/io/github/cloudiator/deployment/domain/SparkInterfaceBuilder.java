package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SparkInterfaceBuilder {

  private String file;
  private String className;
  private List<String> arguments;
  private Map<String, String> sparkArguments;
  private Map<String, String> sparkConfiguration;
  private ProcessMapping processMapping;

  private SparkInterfaceBuilder() {
    arguments = new LinkedList<>();
    sparkArguments = new HashMap<>();
    sparkConfiguration = new HashMap<>();
  }

  public static SparkInterfaceBuilder newBuilder() {
    return new SparkInterfaceBuilder();
  }

  public SparkInterfaceBuilder file(String file) {
    this.file = file;
    return this;
  }

  public SparkInterfaceBuilder className(String className) {
    this.className = className;
    return this;
  }

  public SparkInterfaceBuilder arguments(Iterable<String> arguments) {
    this.arguments = Lists.newLinkedList(arguments);
    return this;
  }

  public SparkInterfaceBuilder addArgument(String argument) {
    checkNotNull(argument, "argument is null");
    arguments.add(argument);
    return this;
  }

  public SparkInterfaceBuilder sparkArguments(Map<String, String> sparkArguments) {
    this.sparkArguments = sparkArguments;
    return this;
  }

  public SparkInterfaceBuilder putSparkArguments(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    sparkArguments.put(key, value);
    return this;
  }

  public SparkInterfaceBuilder sparkConfiguration(Map<String, String> sparkConfiguration) {
    this.sparkConfiguration = sparkConfiguration;
    return this;
  }

  public SparkInterfaceBuilder putSparkConfiguration(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    sparkConfiguration.put(key, value);
    return this;
  }

  public SparkInterfaceBuilder processMapping(ProcessMapping processMapping) {
    this.processMapping = processMapping;
    return this;
  }


  public SparkInterface build() {
    return new SparkInterfaceImpl(file, className, arguments, sparkArguments, sparkConfiguration,
        processMapping);
  }


}
