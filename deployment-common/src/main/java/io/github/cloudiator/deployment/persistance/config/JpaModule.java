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

package io.github.cloudiator.deployment.persistance.config;

import com.google.inject.AbstractModule;
import io.github.cloudiator.deployment.persistance.repositories.CommunicationModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.CommunicationModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.repositories.JobModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.JobModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.repositories.PortModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.PortModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.repositories.ProcessModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.ProcessModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.repositories.TaskInterfaceModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.TaskInterfaceModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.repositories.TaskModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.TaskModelRepositoryJpa;

/**
 * Created by daniel on 31.05.17.
 */
public class JpaModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(CommunicationModelRepository.class).to(CommunicationModelRepositoryJpa.class);
    bind(JobModelRepository.class).to(JobModelRepositoryJpa.class);
    bind(PortModelRepository.class).to(PortModelRepositoryJpa.class);
    bind(ProcessModelRepository.class).to(ProcessModelRepositoryJpa.class);
    bind(TaskInterfaceModelRepository.class).to(TaskInterfaceModelRepositoryJpa.class);
    bind(TaskModelRepository.class).to(TaskModelRepositoryJpa.class);


  }
}
