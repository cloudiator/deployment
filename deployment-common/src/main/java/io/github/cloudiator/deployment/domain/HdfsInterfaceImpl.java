package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.deployment.security.VariableContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class HdfsInterfaceImpl implements HdfsInterface {

  private final String file;
  @Nullable
  private final String className;
  private final List<String> arguments;
  private final Map<String, String> hdfsArguments;
  private final Map<String, String> hdfsConfiguration;
  private final ProcessMapping processMapping;

  public HdfsInterfaceImpl(String file, @Nullable String className,
      List<String> arguments, Map<String, String> hdfsArguments,
      Map<String, String> hdfsConfiguration,
      ProcessMapping processMapping) {

    checkNotNull(file, "file is null");
    checkArgument(!file.isEmpty(), "file is empty");
    this.file = file;

    if (className != null) {
      checkArgument(!className.isEmpty(), "class name is empty");
    }
    this.className = className;
    checkNotNull(arguments, "arguments is null");
    this.arguments = arguments;
    checkNotNull(hdfsArguments, "hdfs arguments is null");
    this.hdfsArguments = hdfsArguments;
    checkNotNull(hdfsConfiguration, "hdfs configuration is null");
    this.hdfsConfiguration = hdfsConfiguration;

    checkNotNull(processMapping, "processMapping is null");
    this.processMapping = processMapping;

  }


  @Override
  public String file() {
    return file;
  }

  @Override
  public Optional<String> className() {
    return Optional.ofNullable(className);
  }

  @Override
  public List<String> arguments() {
    return arguments;
  }

  @Override
  public Map<String, String> hdfsArguments() {
    return hdfsArguments;
  }

  @Override
  public Map<String, String> hdfsConfiguration() {
    return hdfsConfiguration;
  }

  @Override
  public ProcessMapping processMapping() {
    return processMapping;
  }

  @Override
  public boolean isStaticallyConfigured() {
    return true;
  }

  @Override
  public boolean requiresManualWait(TaskInterface dependency) {
    return true;
  }

  @Override
  public TaskInterface decorateEnvironment(Environment environment) {

    final HdfsInterfaceBuilder hdfsInterfaceBuilder = HdfsInterfaceBuilder.of(this);
    environment.forEach((key, value) -> {
      hdfsInterfaceBuilder.addArgument("--" + key);
      hdfsInterfaceBuilder.addArgument(value);
    });

    return hdfsInterfaceBuilder.build();
  }

  @Override
  public TaskInterface decorateVariables(VariableContext variableContext) {
    return HdfsInterfaceBuilder.of(this).decorate(variableContext).build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("file", file).add("className", className)
        .add("arguments", arguments).add("hdfsArguments", hdfsArguments)
        .add("hdfsConfiguration", hdfsConfiguration).add("processMapping", processMapping)
        .toString();
  }
}
