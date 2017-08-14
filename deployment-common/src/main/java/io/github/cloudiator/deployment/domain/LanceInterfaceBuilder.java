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

  private LanceInterfaceBuilder() {
  }

  public static LanceInterfaceBuilder newBuilder() {
    return new LanceInterfaceBuilder();
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

  public LanceInterface build() {
    return new LanceInterfaceImpl(init, preInstall, install, postInstall, preStart, start,
        startDetection, stopDetection, postStart, preStop, stop, postStop, shutdown);
  }


}
