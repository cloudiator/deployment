package io.github.cloudiator.deployment.domain;

public class HttpTriggerBuilder {

  private String httpMethod;
  private String httpPath;

  private HttpTriggerBuilder() {
  }

  public static HttpTriggerBuilder newBuilder() {
    return new HttpTriggerBuilder();
  }

  public HttpTriggerBuilder httpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
    return this;
  }

  public HttpTriggerBuilder httpPath(String httpPath) {
    this.httpPath = httpPath;
    return this;
  }

  public HttpTrigger build() {
    return new HttpTriggerImpl(httpMethod, httpPath);
  }
}
