/*
 * Copyright 2017 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.domain;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import io.github.cloudiator.deployment.security.VariableContext;

public class LanceInterfaceBuilder {

  private String init;
  private String preInstall;
  private String install;
  private String postInstall;
  private String preStart;
  private String start;
  private String startDetection;
  private String stopDetection;
  private String postStart;
  private String preStop;
  private String stop;
  private String postStop;
  private String shutdown;
  private String portUpdateAction;
  private LanceContainerType containerType;
  private OperatingSystem operatingSystem;

  private LanceInterfaceBuilder() {
  }

  private LanceInterfaceBuilder(LanceInterface lanceInterface) {
    init = lanceInterface.init().orElse(null);
    preInstall = lanceInterface.preInstall().orElse(null);
    install = lanceInterface.install().orElse(null);
    postInstall = lanceInterface.postInstall().orElse(null);
    preStart = lanceInterface.preStart().orElse(null);
    start = lanceInterface.start();
    startDetection = lanceInterface.startDetection().orElse(null);
    stopDetection = lanceInterface.stopDetection().orElse(null);
    postStart = lanceInterface.postStart().orElse(null);
    preStop = lanceInterface.preStop().orElse(null);
    stop = lanceInterface.stop().orElse(null);
    postStop = lanceInterface.postStop().orElse(null);
    shutdown = lanceInterface.shutdown().orElse(null);
    portUpdateAction = lanceInterface.portUpdateAction().orElse(null);
    containerType = lanceInterface.containerType();
    operatingSystem = lanceInterface.operatingSystem();
  }

  public static LanceInterfaceBuilder newBuilder() {
    return new LanceInterfaceBuilder();
  }

  public static LanceInterfaceBuilder of(LanceInterface lanceInterface) {
    return new LanceInterfaceBuilder(lanceInterface);
  }

  public LanceInterfaceBuilder containerType(LanceContainerType lanceContainerType) {
    this.containerType = lanceContainerType;
    return this;
  }

  public LanceInterfaceBuilder init(String init) {
    this.init = init;
    return this;
  }

  public LanceInterfaceBuilder preInstall(String preInstall) {
    this.preInstall = preInstall;
    return this;
  }

  public LanceInterfaceBuilder install(String install) {
    this.install = install;
    return this;
  }

  public LanceInterfaceBuilder postInstall(String postInstall) {
    this.postInstall = postInstall;
    return this;
  }

  public LanceInterfaceBuilder preStart(String preStart) {
    this.preStart = preStart;
    return this;
  }

  public LanceInterfaceBuilder start(String start) {
    this.start = start;
    return this;
  }

  public LanceInterfaceBuilder startDetection(String startDetection) {
    this.startDetection = startDetection;
    return this;
  }

  public LanceInterfaceBuilder stopDetection(String stopDetection) {
    this.stopDetection = stopDetection;
    return this;
  }

  public LanceInterfaceBuilder postStart(String postStart) {
    this.postStart = postStart;
    return this;
  }

  public LanceInterfaceBuilder preStop(String preStop) {
    this.preStop = preStop;
    return this;
  }

  public LanceInterfaceBuilder stop(String stop) {
    this.stop = stop;
    return this;
  }

  public LanceInterfaceBuilder postStop(String postStop) {
    this.postStop = postStop;
    return this;
  }

  public LanceInterfaceBuilder shutdown(String shutdown) {
    this.shutdown = shutdown;
    return this;
  }

  public LanceInterfaceBuilder portUpdateAction(String portUpdateAction) {
    this.portUpdateAction = portUpdateAction;
    return this;
  }

  public LanceInterfaceBuilder os(OperatingSystem os) {
    this.operatingSystem = os;
    return this;
  }

  public LanceInterfaceBuilder decorate(VariableContext variableContext) {
    init = variableContext.parse(init);
    preInstall = variableContext.parse(preInstall);
    install = variableContext.parse(install);
    postInstall = variableContext.parse(postInstall);
    preStart = variableContext.parse(preStart);
    start = variableContext.parse(start);
    startDetection = variableContext.parse(startDetection);
    stopDetection = variableContext.parse(stopDetection);
    postStart = variableContext.parse(postStart);
    preStop = variableContext.parse(preStop);
    stop = variableContext.parse(stop);
    postStop = variableContext.parse(postStop);
    shutdown = variableContext.parse(shutdown);
    portUpdateAction = variableContext.parse(portUpdateAction);
    return this;
  }

  public LanceInterface build() {
    return new LanceInterfaceImpl(containerType, operatingSystem, init, preInstall, install,
        postInstall, preStart,
        start,
        startDetection, stopDetection, postStart, preStop, stop, postStop, shutdown,
        portUpdateAction);
  }


}
