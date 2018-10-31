package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class SparkInterfaceImpl implements SparkInterface {

  private final String file;
  @Nullable
  private final String className;
  private final Set<String> arguments;
  private final Map<String, String> sparkArguments;
  private final Map<String, String> sparkConfiguration;

  public SparkInterfaceImpl(String file, @Nullable String className,
      Set<String> arguments, Map<String, String> sparkArguments,
      Map<String, String> sparkConfiguration) {

    checkNotNull(file, "file is null");
    checkArgument(!file.isEmpty(), "file is empty");
    this.file = file;

    if (className != null) {
      checkArgument(!className.isEmpty(), "className is empty");
    }
    this.className = className;
    checkNotNull(arguments, "arguments is null");
    this.arguments = arguments;
    checkNotNull(sparkArguments, "sparkArguments is null");
    this.sparkArguments = sparkArguments;
    checkNotNull(sparkConfiguration, "sparkConfiguration is null");
    this.sparkConfiguration = sparkConfiguration;
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
  public Set<String> arguments() {
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
}