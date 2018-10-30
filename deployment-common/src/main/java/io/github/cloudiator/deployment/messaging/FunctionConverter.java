package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.deployment.domain.FunctionBuilder;
import org.cloudiator.messages.entities.FaasEntities;

public class FunctionConverter implements
    TwoWayConverter<FaasEntities.Function, Function> {

  @Override
  public FaasEntities.Function applyBack(Function function) {
    return FaasEntities.Function.newBuilder()
        .setId(function.id())
        .setCloudId(function.cloudId())
        .setLocationId(function.locationId())
        .setMemory(function.memory())
        .build();
  }

  @Override
  public Function apply(FaasEntities.Function function) {
    return FunctionBuilder.newBuilder()
        .id(function.getId())
        .cloudId(function.getCloudId())
        .locationId(function.getLocationId())
        .memory(function.getMemory())
        .build();
  }
}
