package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class SparkInterfaceImpl implements SparkInterface {

  private final String file;
  @Nullable
  private final String className;
  private final List<String> arguments;
  private final Map<String, String> sparkArguments;
  private final Map<String, String> sparkConfiguration;
  private final ProcessMapping processMapping;

  public SparkInterfaceImpl(String file, @Nullable String className,
      List<String> arguments, Map<String, String> sparkArguments,
      Map<String, String> sparkConfiguration,
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
    checkNotNull(sparkArguments, "sparkArguments is null");
    this.sparkArguments = sparkArguments;
    checkNotNull(sparkConfiguration, "sparkConfiguration is null");
    this.sparkConfiguration = sparkConfiguration;

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
  public Map<String, String> sparkArguments() {
    return sparkArguments;
  }

  @Override
  public Map<String, String> sparkConfiguration() {
    return sparkConfiguration;
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
  public boolean requiresEnvironmentHandling(TaskInterface dependency) {
    return true;
  }

  @Override
  public TaskInterface decorateEnvironment(Environment environment) {

    final SparkInterfaceBuilder sparkInterfaceBuilder = SparkInterfaceBuilder.of(this);
    environment.forEach((key, value) -> {
      sparkInterfaceBuilder.addArgument("--" + key);
      sparkInterfaceBuilder.addArgument(value);
    });

    return sparkInterfaceBuilder.build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("file", file).add("className", className)
        .add("arguments", arguments).add("sparkArguments", sparkArguments)
        .add("sparkConfiguration", sparkConfiguration).add("processMapping", processMapping)
        .toString();
  }
}
