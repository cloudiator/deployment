package io.github.cloudiator.deployment.faasagent.deployment;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer.FaasDeployerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class CompositeFaasDeployerFactory implements FaasDeployerFactory {

  private final Set<FaasDeployerFactory> deployerFactories;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeFaasDeployerFactory.class);

  @Inject
  public CompositeFaasDeployerFactory(Set<FaasDeployerFactory> deployerFactories) {
    this.deployerFactories = deployerFactories;
  }

  @Override
  public boolean supports(Cloud cloud) {
    return true;
  }

  @Override
  public FaasDeployer create(String region, Cloud cloud) {
    for (FaasDeployerFactory factory : deployerFactories) {
      if (factory.supports(cloud)) {
        LOGGER.info("{} chosen {} for cloud {}", this, factory, cloud);
        return factory.create(region, cloud);
      }
    }
    throw new IllegalStateException("Unsupported cloud " + cloud);
  }
}
