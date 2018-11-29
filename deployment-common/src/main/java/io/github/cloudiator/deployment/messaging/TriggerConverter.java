package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.HttpTrigger;
import io.github.cloudiator.deployment.domain.Trigger;
import org.cloudiator.messages.entities.TaskEntities;

public class TriggerConverter implements
    TwoWayConverter<TaskEntities.Trigger, Trigger> {

  public static final TriggerConverter INSTANCE = new TriggerConverter();

  private static final HttpTriggerConverter HTTP_TRIGGER_CONVERTER = HttpTriggerConverter.INSTANCE;

  private TriggerConverter() {

  }

  @Override
  public TaskEntities.Trigger applyBack(Trigger trigger) {
    if (trigger instanceof HttpTrigger) {
      return TaskEntities.Trigger.newBuilder()
          .setHttpTrigger(HTTP_TRIGGER_CONVERTER.applyBack(
              (HttpTrigger) trigger)).build();
    } else {
      throw new AssertionError(
          "Unknown trigger type: " + trigger.getClass().getName());
    }
  }

  @Override
  public Trigger apply(TaskEntities.Trigger trigger) {
    switch (trigger.getTriggerCase()) {
      case HTTPTRIGGER:
        return HTTP_TRIGGER_CONVERTER.apply(trigger.getHttpTrigger());
      case TRIGGER_NOT_SET:
        throw new AssertionError("Trigger is not set");
      default:
        throw new AssertionError(
            "Unknown TriggerCase " + trigger.getTriggerCase());
    }
  }
}
