package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.deployment.domain.FunctionBuilder;
import io.github.cloudiator.messaging.RuntimeConverter;
import org.cloudiator.messages.entities.FaasEntities;


public class FunctionConverter implements TwoWayConverter<FaasEntities.Function, Function> {

  private final RuntimeConverter runtimeConverter = RuntimeConverter.INSTANCE;

  @Override
  public FaasEntities.Function applyBack(Function function) {
    return FaasEntities.Function.newBuilder()
        .setId(function.id())
        .setCloudId(function.cloudId())
        .setLocationId(function.locationId())
        .setMemory(function.memory())
        .setRuntime(runtimeConverter.applyBack(function.runtime()))
        .setStackId(function.stackId())
        .build();
  }

  @Override
  public Function apply(FaasEntities.Function function) {
    return FunctionBuilder.newBuilder()
        .id(function.getId())
        .cloudId(function.getCloudId())
        .locationId(function.getLocationId())
        .memory(function.getMemory())
        .runtime(runtimeConverter.apply(function.getRuntime()))
        .stackId(function.getStackId())
        .build();
  }
}
