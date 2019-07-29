package io.github.cloudiator.deployment.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.ProcessMapping;
import io.github.cloudiator.deployment.domain.HdfsInterface;
import io.github.cloudiator.deployment.domain.HdfsInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.HdfsInterface.Builder;

public class HdfsInterfaceConverter implements
    TwoWayConverter<TaskEntities.HdfsInterface, HdfsInterface> {

  public static final HdfsInterfaceConverter INSTANCE = new HdfsInterfaceConverter();
  public static final ProcessMappingConverter PROCESS_MAPPING_CONVERTER = new ProcessMappingConverter();

  private HdfsInterfaceConverter() {
  }

  @Override
  public TaskEntities.HdfsInterface applyBack(HdfsInterface hdfsInterface) {
    Builder builder = TaskEntities.HdfsInterface.newBuilder();

    if (hdfsInterface.className().isPresent()) {
      builder.setClassName(hdfsInterface.className().get());
    }

    builder.setFile(hdfsInterface.file()).addAllArguments(hdfsInterface.arguments())
        .putAllHdfsArguments(hdfsInterface.hdfsArguments())
        .putAllHdfsConfiguration(hdfsInterface.hdfsConfiguration());

    builder.setProcessMapping(PROCESS_MAPPING_CONVERTER.applyBack(hdfsInterface.processMapping()));

    return builder.build();
  }

  @Override
  public HdfsInterface apply(TaskEntities.HdfsInterface hdfsInterface) {

    final HdfsInterfaceBuilder hdfsInterfaceBuilder = HdfsInterfaceBuilder.newBuilder()
        .file(hdfsInterface.getFile())

        .arguments(hdfsInterface.getArgumentsList())
        .hdfsArguments(hdfsInterface.getHdfsArgumentsMap())
        .hdfsConfiguration(hdfsInterface.getHdfsConfigurationMap());

    if (!Strings.isNullOrEmpty(hdfsInterface.getClassName())) {
      hdfsInterfaceBuilder.className(hdfsInterface.getClassName());
    }

    hdfsInterfaceBuilder
        .processMapping(PROCESS_MAPPING_CONVERTER.apply(hdfsInterface.getProcessMapping()));

    return hdfsInterfaceBuilder.build();
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
