package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Job;
import javax.annotation.Nullable;

class JobModelConverter implements OneWayConverter<JobModel,Job> {

  @Nullable
  @Override
  public Job apply(@Nullable JobModel jobModel) {
    return null;
  }
}
