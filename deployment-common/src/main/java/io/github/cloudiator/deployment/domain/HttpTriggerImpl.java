package io.github.cloudiator.deployment.domain;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class HttpTriggerImpl implements HttpTrigger {

  private final String httpMethod;
  private final String httpPath;

  HttpTriggerImpl(
      String httpMethod,
      String httpPath
  ) {
    checkNotNull(httpMethod, "httpMethod is null");
    checkNotNull(httpPath, "httpPath is null");
    this.httpMethod = httpMethod;
    this.httpPath = httpPath;
  }


  @Override
  public String httpMethod() {
    return httpMethod;
  }

  @Override
  public String httpPath() {
    return httpPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpTriggerImpl that = (HttpTriggerImpl) o;
    return
        Objects.equals(httpMethod, that.httpMethod) &&
        Objects.equals(httpPath, that.httpPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpMethod, httpPath);
  }

  @Override
  public String toString() {
    return "HttpTriggerImpl{" +
        "httpMethod='" + httpMethod + '\'' +
        ", httpPath='" + httpPath+ '\'' +
        "}";
  }
}
