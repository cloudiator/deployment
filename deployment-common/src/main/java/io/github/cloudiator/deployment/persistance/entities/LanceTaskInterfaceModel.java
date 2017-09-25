package io.github.cloudiator.deployment.persistance.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class LanceTaskInterfaceModel extends TaskInterfaceModel {

  @Nullable
  private String init;
  @Nullable
  private String preInstall;
  @Nullable
  private String install;
  @Nullable
  private String postInstall;
  @Nullable
  private String preStart;
  @Column(nullable = false)
  private String start;
  @Nullable
  private String startDetection;
  @Nullable
  private String stopDetection;
  @Nullable
  private String postStart;
  @Nullable
  private String preStop;
  @Nullable
  private String stop;
  @Nullable
  private String postStop;
  @Nullable
  private String shutdown;

  protected LanceTaskInterfaceModel() {

  }

  public LanceTaskInterfaceModel(@Nullable String init, @Nullable String preInstall,
      @Nullable String install,
      @Nullable String postInstall, @Nullable String preStart, String start,
      @Nullable String startDetection,
      @Nullable String stopDetection, @Nullable String postStart, @Nullable String preStop,
      @Nullable String stop, @Nullable String postStop,
      @Nullable String shutdown) {
    this.init = init;
    this.preInstall = preInstall;
    this.install = install;
    this.postInstall = postInstall;
    this.preStart = preStart;
    checkNotNull(start, "start is null");
    checkArgument(!start.isEmpty(), "start is empty");
    this.start = start;
    this.startDetection = startDetection;
    this.stopDetection = stopDetection;
    this.postStart = postStart;
    this.preStop = preStop;
    this.stop = stop;
    this.postStop = postStop;
    this.shutdown = shutdown;
  }

  @Nullable
  public String getInit() {
    return init;
  }

  @Nullable
  public String getPreInstall() {
    return preInstall;
  }

  @Nullable
  public String getInstall() {
    return install;
  }

  @Nullable
  public String getPostInstall() {
    return postInstall;
  }

  @Nullable
  public String getPreStart() {
    return preStart;
  }

  public String getStart() {
    return start;
  }

  @Nullable
  public String getStartDetection() {
    return startDetection;
  }

  @Nullable
  public String getStopDetection() {
    return stopDetection;
  }

  @Nullable
  public String getPostStart() {
    return postStart;
  }

  @Nullable
  public String getPreStop() {
    return preStop;
  }

  @Nullable
  public String getStop() {
    return stop;
  }

  @Nullable
  public String getPostStop() {
    return postStop;
  }

  @Nullable
  public String getShutdown() {
    return shutdown;
  }
}
