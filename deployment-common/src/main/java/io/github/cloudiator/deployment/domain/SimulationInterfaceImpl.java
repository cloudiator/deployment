/*
 * Copyright 2014-2019 University of Ulm
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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.security.VariableContext;

public class SimulationInterfaceImpl implements SimulationInterface {

  private final Distribution startTime;

  public SimulationInterfaceImpl(Distribution startTime) {
    checkNotNull(startTime, "startTime is null");
    this.startTime = startTime;
  }

  @Override
  public Distribution startTime() {
    return startTime;
  }

  @Override
  public ProcessMapping processMapping() {
    return ProcessMapping.SINGLE;
  }

  @Override
  public boolean isStaticallyConfigured() {
    return false;
  }

  @Override
  public boolean requiresManualWait(TaskInterface dependency) {
    return false;
  }

  @Override
  public TaskInterface decorateEnvironment(Environment environment) {
    return this;
  }

  @Override
  public TaskInterface decorateVariables(VariableContext variableContext) {
    return this;
  }
}
