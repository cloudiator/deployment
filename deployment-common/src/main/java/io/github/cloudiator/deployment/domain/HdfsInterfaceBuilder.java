package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import io.github.cloudiator.deployment.security.VariableContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HdfsInterfaceBuilder {

  private String file;
  private String className;
  private List<String> arguments;
  private Map<String, String> hdfsArguments;
  private Map<String, String> hdfsConfiguration;
  private ProcessMapping processMapping;

  private HdfsInterfaceBuilder() {
    arguments = new LinkedList<>();
    hdfsArguments = new HashMap<>();
    hdfsConfiguration = new HashMap<>();
  }

  private HdfsInterfaceBuilder(HdfsInterface hdfsInterface) {
    file = hdfsInterface.file();
    className = hdfsInterface.className().orElse(null);
    arguments = new LinkedList<>(hdfsInterface.arguments());
    hdfsArguments = new HashMap<>(hdfsInterface.hdfsArguments());
    hdfsConfiguration = new HashMap<>(hdfsInterface.hdfsConfiguration());
    processMapping = hdfsInterface.processMapping();
  }

  public static HdfsInterfaceBuilder newBuilder() {
    return new HdfsInterfaceBuilder();
  }

  public static HdfsInterfaceBuilder of(HdfsInterface hdfsInterface) {
    return new HdfsInterfaceBuilder(hdfsInterface);
  }

  public HdfsInterfaceBuilder file(String file) {
    this.file = file;
    return this;
  }

  public HdfsInterfaceBuilder className(String className) {
    this.className = className;
    return this;
  }

  public HdfsInterfaceBuilder arguments(Iterable<String> arguments) {
    this.arguments = Lists.newLinkedList(arguments);
    return this;
  }

  public HdfsInterfaceBuilder addArgument(String argument) {
    checkNotNull(argument, "argument is null");
    arguments.add(argument);
    return this;
  }

  public HdfsInterfaceBuilder hdfsArguments(Map<String, String> hdfsArguments) {
    this.hdfsArguments = hdfsArguments;
    return this;
  }

  public HdfsInterfaceBuilder putHdfsArguments(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    hdfsArguments.put(key, value);
    return this;
  }

  public HdfsInterfaceBuilder hdfsConfiguration(Map<String, String> hdfsConfiguration) {
    this.hdfsConfiguration = hdfsConfiguration;
    return this;
  }

  public HdfsInterfaceBuilder putHdfsConfiguration(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    hdfsConfiguration.put(key, value);
    return this;
  }

  public HdfsInterfaceBuilder processMapping(ProcessMapping processMapping) {
    this.processMapping = processMapping;
    return this;
  }

  public HdfsInterfaceBuilder decorate(VariableContext variableContext) {

    arguments.replaceAll(variableContext::parse);
    hdfsArguments.replaceAll((k, v) -> variableContext.parse(v));
    hdfsConfiguration.replaceAll((k, v) -> variableContext.parse(v));

    return this;
  }


  public HdfsInterface build() {
    return new HdfsInterfaceImpl(file, className, arguments, hdfsArguments, hdfsConfiguration,
        processMapping);
  }


}
