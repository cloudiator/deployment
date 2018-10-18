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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.List;
import javax.persistence.EntityManager;

class ProcessModelRepositoryJpa extends BaseModelRepositoryJpa<ProcessModel> implements
    ProcessModelRepository {

  @Inject
  protected ProcessModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ProcessModel> type) {
    super(entityManager, type);
  }

  @Override
  public List<ProcessModel> findByUser(String userId) {
    String query = String.format(
        "select process from %s as process inner join process.schedule as schedule inner join schedule.tenant as tenant where tenant.userId = :userId",
        type.getName());

    //noinspection unchecked
    return em().createQuery(query).setParameter("userId", userId).getResultList();
  }

  @Override
  public List<ProcessModel> findByScheduleAndUser(String scheduleId, String userId) {
    String query = String.format(
        "select process from %s as process inner join process.schedule as schedule inner join schedule.tenant as tenant where schedule.domainId = :scheduleId and tenant.userId = :userId",
        type.getName());

    //noinspection unchecked
    return em().createQuery(query).setParameter("scheduleId", scheduleId)
        .setParameter("userId", userId).getResultList();
  }

  @Override
  public ProcessModel findByIdAndUser(String id, String userId) {
    String query = String.format(
        "select process from %s as process inner join process.schedule as schedule inner join schedule.tenant as tenant where process.domainId = :id and tenant.userId = :userId",
        type.getName());

    //noinspection unchecked
    return (ProcessModel) JpaResultHelper
        .getSingleResultOrNull(
            em().createQuery(query).setParameter("id", id).setParameter("userId", userId));
  }
}
