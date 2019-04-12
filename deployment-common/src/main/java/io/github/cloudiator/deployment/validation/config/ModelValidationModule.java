/*
 * Copyright (c) 2014-2016 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.deployment.validation.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.validation.EveryPortRequiredFullfilledValidator;
import io.github.cloudiator.deployment.validation.EveryTaskHasAnInterface;
import io.github.cloudiator.deployment.validation.ModelValidationService;
import io.github.cloudiator.deployment.validation.ModelValidationServiceImpl;
import io.github.cloudiator.deployment.validation.ModelValidator;
import io.github.cloudiator.deployment.validation.NoCycleInMandatoryCommunicationValidator;
import io.github.cloudiator.deployment.validation.PortsOfCommunicationsExistValidator;

/**
 * Created by daniel on 14.07.16.
 */
public class ModelValidationModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelValidationService.class).to(ModelValidationServiceImpl.class);
    final Multibinder<ModelValidator<Job>> typeModelValidatorBinder =
        Multibinder.newSetBinder(binder(), new TypeLiteral<ModelValidator<Job>>() {
        });
    typeModelValidatorBinder.addBinding().to(NoCycleInMandatoryCommunicationValidator.class);
    typeModelValidatorBinder.addBinding().to(EveryPortRequiredFullfilledValidator.class);
    typeModelValidatorBinder.addBinding().to(PortsOfCommunicationsExistValidator.class);
    typeModelValidatorBinder.addBinding().to(EveryTaskHasAnInterface.class);
  }
}
