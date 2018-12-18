package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.FaasInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;

import java.util.stream.Collectors;

public class FaasInterfaceConverter implements
    TwoWayConverter<TaskEntities.FaasInterface, FaasInterface> {

  public static final FaasInterfaceConverter INSTANCE = new FaasInterfaceConverter();

  private static final TriggerConverter TRIGGER_CONVERTER = TriggerConverter.INSTANCE;

  private FaasInterfaceConverter() {

  }

  @Override
  public TaskEntities.FaasInterface applyBack(FaasInterface faasInterface) {
    return TaskEntities.FaasInterface.newBuilder()
        .setFunctionName(faasInterface.functionName())
        .setSourceCodeUrl(faasInterface.sourceCodeUrl())
        .setHandler(faasInterface.handler())
     //   .setRuntime(faasInterface.runtime())
        .setTimeout(faasInterface.timeout())
    //    .setMemory(faasInterface.memory())
        .addAllTriggers(faasInterface.triggers().stream()
            .map(TRIGGER_CONVERTER::applyBack)
            .collect(Collectors.toList()))
        .putAllFunctionEnvironment(faasInterface.functionEnvironment())
        .build();
  }

  @Override
  public FaasInterface apply(TaskEntities.FaasInterface faasInterface) {
    return FaasInterfaceBuilder.newBuilder()
        .functionName(faasInterface.getFunctionName())
        .sourceCodeUrl(faasInterface.getSourceCodeUrl())
        .handler(faasInterface.getHandler())
    //    .runtime(faasInterface.getRuntime())
        .timeout(faasInterface.getTimeout())
    //    .memory(faasInterface.getMemory())
        .triggers(faasInterface.getTriggersList().stream()
            .map(TRIGGER_CONVERTER)
            .collect(Collectors.toSet()))
        .functionEnvironment(faasInterface.getFunctionEnvironmentMap())
        .build();
  }
}
