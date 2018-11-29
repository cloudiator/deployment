package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.HttpTrigger;
import io.github.cloudiator.deployment.domain.HttpTriggerBuilder;
import org.cloudiator.messages.entities.TaskEntities;

public class HttpTriggerConverter implements
    TwoWayConverter<TaskEntities.HttpTrigger, HttpTrigger> {

  public static final HttpTriggerConverter INSTANCE = new HttpTriggerConverter();

  private HttpTriggerConverter() {

  }

  @Override
  public TaskEntities.HttpTrigger applyBack(HttpTrigger httpTrigger) {
    return TaskEntities.HttpTrigger.newBuilder()
        .setHttpPath(httpTrigger.httpPath())
        .setHttpMethod(httpTrigger.httpMethod())
        .build();
  }

  @Override
  public HttpTrigger apply(TaskEntities.HttpTrigger httpTrigger) {
    return HttpTriggerBuilder.newBuilder()
        .httpMethod(httpTrigger.getHttpMethod())
        .httpPath(httpTrigger.getHttpPath())
        .build();
  }
}
