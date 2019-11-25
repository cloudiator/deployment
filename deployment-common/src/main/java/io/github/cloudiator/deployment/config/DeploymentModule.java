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

package io.github.cloudiator.deployment.config;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.github.cloudiator.deployment.security.VariableContext;
import io.github.cloudiator.deployment.security.VariableContextFactory;
import io.github.cloudiator.deployment.security.VariableContextImpl;

public class DeploymentModule extends AbstractModule {

  private final DeploymentContext deploymentContext;

  public DeploymentModule(DeploymentContext deploymentContext) {
    this.deploymentContext = deploymentContext;
  }

  @Override
  protected void configure() {

    bindConstant().annotatedWith(Names.named(Constants.INSTALL_MELODIC_TOOLS))
        .to(deploymentContext.installMelodicTools());

    bindConstant().annotatedWith(Names.named(Constants.API)).to(deploymentContext.api());

    install(new FactoryModuleBuilder().implement(VariableContext.class, VariableContextImpl.class)
        .build(VariableContextFactory.class));

  }
}
