/*
 * Copyright 2018 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Schedule;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ScheduleDomainRepository {

  private final ScheduleModelRepository scheduleModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private static final ScheduleModelConverter SCHEDULE_MODEL_CONVERTER = ScheduleModelConverter.INSTANCE;

  @Inject
  ScheduleDomainRepository(
      ScheduleModelRepository scheduleModelRepository,
      TenantModelRepository tenantModelRepository) {
    this.scheduleModelRepository = scheduleModelRepository;
    this.tenantModelRepository = tenantModelRepository;
  }

  public Set<Schedule> findAllByUser(String userId) {
    checkNotNull(userId, "userId is null");
    checkArgument(!userId.isEmpty(), "userId is empty");

    return scheduleModelRepository.findByUser(userId).stream().map(SCHEDULE_MODEL_CONVERTER)
        .collect(Collectors.toSet());
  }

  @Nullable
  public Schedule findByIdAndUser(String scheduleId, String userId) {
    return SCHEDULE_MODEL_CONVERTER
        .apply(scheduleModelRepository.findByIdAndUser(scheduleId, userId));
  }

  public void save(Schedule domain) {
    saveAndGet(domain);
  }

  public void delete(Schedule domain, String userId) {
    final ScheduleModel scheduleModel = scheduleModelRepository
        .findByIdAndUser(domain.id(), userId);

    if (scheduleModel == null) {
      throw new IllegalStateException(
          String.format("Schedule with the id %s does not exist.", domain.id()));
    }

    scheduleModelRepository.delete(scheduleModel);
  }

  ScheduleModel saveAndGet(Schedule domain) {

    //check if exists
    ScheduleModel scheduleModel = scheduleModelRepository
        .findByIdAndUser(domain.id(), domain.userId());

    if (scheduleModel == null) {
      scheduleModel = createScheduleModel(domain);
    } else {
      scheduleModel = updateScheduleModel(domain, scheduleModel);
    }

    scheduleModelRepository.save(scheduleModel);

    return scheduleModel;
  }

  private ScheduleModel updateScheduleModel(Schedule domain, ScheduleModel scheduleModel) {

    checkState(domain.id().equals(scheduleModel.domainId()), "Domain ids are not equal");

    scheduleModel.setState(domain.state());

    return scheduleModel;
  }

  private ScheduleModel createScheduleModel(Schedule domain) {

    final TenantModel tenantModel = tenantModelRepository.createOrGet(domain.userId());

    final ScheduleModel scheduleModel = new ScheduleModel(domain.id(), tenantModel, domain.job(),
        domain.instantiation(), domain.state());

    scheduleModelRepository.save(scheduleModel);

    return scheduleModel;
  }

}
