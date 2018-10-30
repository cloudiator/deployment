package io.github.cloudiator.deployment.faasagent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.cloudiator.deployment.faasagent.config.FaasAgentModule;
import io.github.cloudiator.deployment.faasagent.messaging.CreateFaasProcessSubscriber;
import io.github.cloudiator.deployment.faasagent.messaging.CreateFunctionSubscriber;
import io.github.cloudiator.persistance.DeploymentJpaModule;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class FaasAgent {

  private final static Injector INJECTOR = Guice
      .createInjector(
          new DeploymentJpaModule("defaultPersistenceUnit", new JpaContext()),
          new KafkaMessagingModule(new KafkaContext()), new MessageServiceModule(),
          new FaasAgentModule());

  public static void main(String[] args) {
    INJECTOR.getInstance(CreateFaasProcessSubscriber.class).run();
    INJECTOR.getInstance(CreateFunctionSubscriber.class).run();
  }
}
