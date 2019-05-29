/*
 * Copyright 2018 University of Ulm
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.github.cloudiator.deployment.security.VariableContext;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class DockerInterfaceImpl implements DockerInterface {

  private final String dockerImage;
  private final Map<String, String> environment;
  @Nullable
  private final String portUpdateAction;

  DockerInterfaceImpl(String dockerImage, Map<String, String> environment,
      @Nullable String portUpdateAction) {

    checkNotNull(dockerImage, "dockerImage is null");
    checkArgument(!dockerImage.isEmpty(), "dockerImage is empty");
    this.dockerImage = dockerImage;

    checkNotNull(environment, "environment is null");
    this.environment = ImmutableMap.copyOf(environment);

    this.portUpdateAction = portUpdateAction;
  }

  @Override
  public String dockerImage() {
    return dockerImage;
  }

  @Override
  public Map<String, String> environment() {
    return environment;
  }

  @Override
  public Optional<String> portUpdateAction() {
    return Optional.ofNullable(portUpdateAction);
  }

  @Override
  public ProcessMapping processMapping() {
    return ProcessMapping.SINGLE;
  }

  @Override
  public boolean isStaticallyConfigured() {
    return !portUpdateAction().isPresent();
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
    return DockerInterfaceBuilder.of(this).decorate(variableContext).build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("dockerImage", dockerImage)
        .add("environment", environment).add("portUpdateAction", portUpdateAction).toString();
  }
}
