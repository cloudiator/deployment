package io.github.cloudiator.deployment.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.ProcessMapping;
import io.github.cloudiator.deployment.domain.SparkInterface;
import io.github.cloudiator.deployment.domain.SparkInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.SparkInterface.Builder;

public class SparkInterfaceConverter implements
    TwoWayConverter<TaskEntities.SparkInterface, SparkInterface> {

  public static final SparkInterfaceConverter INSTANCE = new SparkInterfaceConverter();
  public static final ProcessMappingConverter PROCESS_MAPPING_CONVERTER = new ProcessMappingConverter();

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

    builder.setProcessMapping(PROCESS_MAPPING_CONVERTER.applyBack(sparkInterface.processMapping()));

    return builder.build();
  }

  @Override
  public SparkInterface apply(TaskEntities.SparkInterface sparkInterface) {

    final SparkInterfaceBuilder sparkInterfaceBuilder = SparkInterfaceBuilder.newBuilder()
        .file(sparkInterface.getFile())

        .arguments(sparkInterface.getArgumentsList())
        .sparkArguments(sparkInterface.getSparkArgumentsMap())
        .sparkConfiguration(sparkInterface.getSparkConfigurationMap());

    if (!Strings.isNullOrEmpty(sparkInterface.getClassName())) {
      sparkInterfaceBuilder.className(sparkInterface.getClassName());
    }

    sparkInterfaceBuilder
        .processMapping(PROCESS_MAPPING_CONVERTER.apply(sparkInterface.getProcessMapping()));

    return sparkInterfaceBuilder.build();
  }

  private static class ProcessMappingConverter implements
      TwoWayConverter<TaskEntities.ProcessMapping, ProcessMapping> {

    @Override
    public TaskEntities.ProcessMapping applyBack(ProcessMapping processMapping) {
      switch (processMapping) {
        case SINGLE:
          return TaskEntities.ProcessMapping.SINGLE;
        case CLUSTER:
          return TaskEntities.ProcessMapping.CLUSTER;
        default:
          throw new AssertionError("Unknown ProcessMapping type " + processMapping);
      }
    }

    @Override
    public ProcessMapping apply(TaskEntities.ProcessMapping processMapping) {
      switch (processMapping) {
        case CLUSTER:
          return ProcessMapping.CLUSTER;
        case SINGLE:
          return ProcessMapping.SINGLE;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown process mapping type " + processMapping);
      }
    }
  }
}
