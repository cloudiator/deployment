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

public class CommunicationBuilder {


  private String portProvided;
  private String portRequired;

  private CommunicationBuilder() {
  }

  public static CommunicationBuilder newBuilder() {
    return new CommunicationBuilder();
  }

  public CommunicationBuilder portProvided(String portProvided) {
    this.portProvided = portProvided;
    return this;
  }

  public CommunicationBuilder portRequired(String portRequired) {
    this.portRequired = portRequired;
    return this;
  }

  public Communication build() {
    return new CommunicationImpl(portProvided, portRequired);
  }

}
