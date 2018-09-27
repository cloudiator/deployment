package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.SparkInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.SparkInterface.Builder;

public class SparkInterfaceConverter implements
    TwoWayConverter<TaskEntities.SparkInterface, SparkInterface> {

  public static final SparkInterfaceConverter INSTANCE = new SparkInterfaceConverter();

  private SparkInterfaceConverter() {
  }

  @Override
  public TaskEntities.SparkInterface applyBack(SparkInterface sparkInterface) {
    Builder builder = TaskEntities.SparkInterface.newBuilder();

    if (sparkInterface.className().isPresent()) {
      builder.setClassName(sparkInterface.className().get());
    }

    builder.setFile(sparkInterface.file()).addAllArguments(sparkInterface.arguments())
        .putAllSparkArguments(sparkInterface.sparkArguments())
        .putAllSparkConfiguration(sparkInterface.sparkConfiguration());

    return builder.build();
  }

  @Override
  public SparkInterface apply(TaskEntities.SparkInterface sparkInterface) {

    return SparkInterfaceBuilder.newBuilder().file(sparkInterface.getFile())
        .className(sparkInterface.getClassName())
        .arguments(sparkInterface.getArgumentsList())
        .sparkArguments(sparkInterface.getSparkArgumentsMap())
        .sparkConfiguration(sparkInterface.getSparkConfigurationMap()).build();

  }
}
