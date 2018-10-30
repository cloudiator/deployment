package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.deployment.domain.FunctionBuilder;

import javax.annotation.Nullable;

public class FunctionModelConverter implements
    OneWayConverter<FunctionModel, Function> {

  FunctionModelConverter() {
  }

  @Nullable
  @Override
  public Function apply(@Nullable FunctionModel functionModel) {
    if (functionModel == null) {
      return null;
    }
    return FunctionBuilder.newBuilder()
        .id(functionModel.getFunctionId())
        .cloudId(functionModel.getCloudId())
        .locationId(functionModel.getLocationId())
        .memory(functionModel.getMemory())
        .build();
  }
}
