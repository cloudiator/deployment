package io.github.cloudiator.deployment.faasagent.cloudformation.models;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

public class LambdaTemplate {
  private static final Set<String> HTTP = ImmutableSet.of(
      "get", "post", "put", "delete", "head", "options", "patch");
  public String name; // FaasInterface name
  public String codeUrl; // FaasInterface sourceCodeUrl
  public String s3CodeKey;
  public String httpPath; // From trigger
  public String httpMethod; // From trigger
  public String handler; // FaasInterface handler
  public int memorySize = 1024; // FaasInterface memory
  public int timeout = 6; // FaasInterface timeout
  public String runtime; // FaasInterface runtime

  public boolean isValid() {
    return name.matches("\\w+") &&
        httpPath.matches("[a-z]+(/[a-z]+)+") &&
        HTTP.contains(httpMethod.toLowerCase()) &&
        handler.matches("\\w+(/\\w+)+\\.\\w+") &&
        memorySize >= 128 &&
        memorySize <= 3008 &&
        timeout > 0 &&
        timeout <= 300;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        httpPath,
        httpMethod,
        handler,
        memorySize,
        timeout);
  }
}
