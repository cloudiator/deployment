package io.github.cloudiator.deployment.faasagent.config;

import com.google.inject.AbstractModule;
import io.github.cloudiator.deployment.faasagent.Init;

public class FaasAgentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Init.class).asEagerSingleton();
    //todo inject differrent deployer for each provider
  }
}
