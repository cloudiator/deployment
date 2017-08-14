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

public class PortRequiredBuilder {

  private String name;
  private String updateAction;
  private boolean isMandatory;

  private PortRequiredBuilder() {
  }

  public static PortRequiredBuilder newBuilder() {
    return new PortRequiredBuilder();
  }

  public PortRequiredBuilder name(String name) {
    this.name = name;
    return this;
  }

  public PortRequiredBuilder updateAction(String updateAction) {
    this.updateAction = updateAction;
    return this;
  }

  public PortRequiredBuilder isMandatory(boolean isMandatory) {
    this.isMandatory = isMandatory;
    return this;
  }

  public PortRequired build() {
    return new PortRequiredImpl(name, updateAction, isMandatory);
  }

}
