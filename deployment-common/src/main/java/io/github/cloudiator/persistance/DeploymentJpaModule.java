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

package io.github.cloudiator.persistance;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.util.JpaContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 31.05.17.
 */
public class DeploymentJpaModule extends AbstractModule {

  private final String jpaUnit;
  private final JpaContext jpaContext;

  public DeploymentJpaModule(String jpaUnit, JpaContext jpaContext) {
    this.jpaUnit = jpaUnit;
    this.jpaContext = jpaContext;
  }

  @Override
  protected void configure() {

    install(buildPersistModule());

    bind(CommunicationModelRepository.class).to(CommunicationModelRepositoryJpa.class);
    bind(JobModelRepository.class).to(JobModelRepositoryJpa.class);
    bind(PortModelRepository.class).to(PortModelRepositoryJpa.class);
    bind(TaskModelRepository.class).to(TaskModelRepositoryJpa.class);
    bind(TenantModelRepository.class).to(TenantModelRepositoryJpa.class);
    bind(TaskInterfaceModelRepository.class).to(TaskInterfaceModelRepositoryJpa.class);
    bind(OptimizationModelRepository.class).to(OptimizationModelRepositoryJpa.class);
    bind(RequirementModelRepository.class).to(RequirementModelRepositoryJpa.class);
    bind(TriggerModelRepository.class).to(TriggerModelRepositoryJpa.class);
    bind(ScheduleModelRepository.class).to(ScheduleModelRepositoryJpa.class);
    bind(ProcessModelRepository.class).to(ProcessModelRepositoryJpa.class);
    bind(NodeGroupModelRepository.class).to(NodeGroupModelRepositoryJpa.class);
    bind(ProcessGroupModelRepository.class).to(ProcessGroupModelRepositoryJpa.class);
    bind(FunctionModelRepository.class).to(FunctionModelRepositoryJpa.class);

  }

  private JpaPersistModule buildPersistModule() {
    final JpaPersistModule jpaPersistModule = new JpaPersistModule(jpaUnit);
    Map<String, String> config = new HashMap<>();
    config.put("hibernate.dialect", jpaContext.dialect());
    config.put("javax.persistence.jdbc.driver", jpaContext.driver());
    config.put("javax.persistence.jdbc.url", jpaContext.url());
    config.put("javax.persistence.jdbc.user", jpaContext.user());
    config.put("javax.persistence.jdbc.password", jpaContext.password());
    jpaPersistModule.properties(config);
    return jpaPersistModule;
  }
}
