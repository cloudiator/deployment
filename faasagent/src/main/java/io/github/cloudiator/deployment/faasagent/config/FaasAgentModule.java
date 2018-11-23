package io.github.cloudiator.deployment.faasagent.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.github.cloudiator.deployment.faasagent.Init;
import io.github.cloudiator.deployment.faasagent.cloudformation.AwsDeployer;
import io.github.cloudiator.deployment.faasagent.cloudformation.AwsDeployer.AwsDeployerFactory;
import io.github.cloudiator.deployment.faasagent.deployment.CompositeFaasDeployerFactory;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer.FaasDeployerFactory;

public class FaasAgentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Init.class).asEagerSingleton();
    bind(FaasDeployerFactory.class).to(CompositeFaasDeployerFactory.class);

    Multibinder<FaasDeployerFactory> faasDeployerMultibinder =
        Multibinder.newSetBinder(binder(), FaasDeployerFactory.class);
    faasDeployerMultibinder.addBinding().to(AwsDeployerFactory.class);
  }
}
