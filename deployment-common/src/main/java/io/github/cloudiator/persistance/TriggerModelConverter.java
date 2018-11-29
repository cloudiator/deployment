package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.HttpTrigger;
import io.github.cloudiator.deployment.domain.HttpTriggerBuilder;
import io.github.cloudiator.deployment.domain.Trigger;

import javax.annotation.Nullable;

public class TriggerModelConverter implements
    OneWayConverter<TriggerModel, Trigger> {

  @Nullable
  @Override
  public Trigger apply(@Nullable TriggerModel triggerModel) {
    if (triggerModel == null) {
      return null;
    }

    if (triggerModel instanceof HttpTriggerModel) {
      return httpTrigger((HttpTriggerModel) triggerModel);
    } else {
      throw new AssertionError(
          String.format("triggerModel has illegal type %s.", triggerModel.getClass()));
    }

  }

  private HttpTrigger httpTrigger(HttpTriggerModel httpTriggerModel) {
    return HttpTriggerBuilder.newBuilder()
        .httpMethod(httpTriggerModel.getHttpMethod())
        .httpPath(httpTriggerModel.getHttpPath())
        .build();
  }
}
