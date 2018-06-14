package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import javax.annotation.Nullable;

class JobModelConverter implements OneWayConverter<JobModel, Job> {

  final static JobModelConverter INSTANCE = new JobModelConverter();

  private final static TaskModelConverter TASK_MODEL_CONVERTER = TaskModelConverter.INSTANCE;
  private final static CommunicationModelConverter COMMUNICATION_MODEL_CONVERTER = CommunicationModelConverter.INSTANCE;

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

    jobModel.getTasks().stream().map(TASK_MODEL_CONVERTER)
        .forEach(jobBuilder::addTask);

    jobModel.getCommunications().stream().map(COMMUNICATION_MODEL_CONVERTER)
        .forEach(jobBuilder::addCommunication);

    return jobBuilder.build();
  }
}
