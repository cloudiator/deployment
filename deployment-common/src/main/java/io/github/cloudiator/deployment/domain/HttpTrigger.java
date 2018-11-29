package io.github.cloudiator.deployment.domain;

public interface HttpTrigger extends Trigger {

  String httpMethod();

  String httpPath();

}
