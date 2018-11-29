package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
public class HttpTriggerModel extends TriggerModel {

  @Column(nullable = false)
  private String httpMethod;

  @Column(nullable = false)
  private String httpPath;

  /**
   * Empty constructor for hibernate.
   */
  protected HttpTriggerModel() {
  }

  protected HttpTriggerModel(
      FaasTaskInterfaceModel faasInterface,
      String httpMethod,
      String httpPath) {
    super(faasInterface);
    checkNotNull(httpMethod, "httpMethod is null");
    checkNotNull(httpPath, "httpPath is null");
    this.httpMethod = httpMethod;
    this.httpPath = httpPath;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getHttpPath() {
    return httpPath;
  }

  public void setHttpPath(String httpPath) {
    this.httpPath = httpPath;
  }
}
