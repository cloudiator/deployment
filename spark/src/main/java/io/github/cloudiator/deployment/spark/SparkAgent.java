package io.github.cloudiator.deployment.spark;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.cloudiator.deployment.config.DeploymentContext;
import io.github.cloudiator.deployment.config.DeploymentModule;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

/**
 * Created by Daniel Seybold on 06.11.2018.
 */
public class SparkAgent  {

  private static final Injector INJECTOR = Guice
      .createInjector(
          new MessageServiceModule(),
          new DeploymentModule(new DeploymentContext()),
          new KafkaMessagingModule(new KafkaContext()));


  public static void main(String[] args) {
    INJECTOR.getInstance(CreateSparkProcessSubscriber.class).run();
    INJECTOR.getInstance(CreateSparkClusterSubscriber.class).run();
  }

}
