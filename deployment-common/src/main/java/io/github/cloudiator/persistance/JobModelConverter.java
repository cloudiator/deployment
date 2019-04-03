package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class JobModelConverter implements OneWayConverter<JobModel, Job> {

  final static JobModelConverter INSTANCE = new JobModelConverter();

  private final static TaskModelConverter TASK_MODEL_CONVERTER = TaskModelConverter.INSTANCE;
  private final static CommunicationModelConverter COMMUNICATION_MODEL_CONVERTER = CommunicationModelConverter.INSTANCE;
  private final RequirementModelConverter requirementModelConverter = new RequirementModelConverter();
  private final OptimizationModelConverter optimizationModelConverter = new OptimizationModelConverter();

  private JobModelConverter() {

  }

  @Nullable
  @Override
  public Job apply(@Nullable JobModel jobModel) {

    if (jobModel == null) {
      return null;
    }

    JobBuilder jobBuilder = JobBuilder.newBuilder();
    jobBuilder.name(jobModel.getName());
    jobBuilder.id(jobModel.getUuid());
    jobBuilder.userId(jobModel.getTenant().getUserId());

    jobModel.getTasks().stream().map(TASK_MODEL_CONVERTER)
        .forEach(jobBuilder::addTask);

    jobModel.getCommunications().stream().map(COMMUNICATION_MODEL_CONVERTER)
        .forEach(jobBuilder::addCommunication);

    jobBuilder.addRequirements(jobModel.getRequirements().stream().map(requirementModelConverter)
        .collect(Collectors.toList()));
    jobBuilder.optimization(optimizationModelConverter.apply(jobModel.getOptimizationModel()));

    return jobBuilder.build();
  }
}
