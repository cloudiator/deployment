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

public class ScheduleModelRepositoryJpa extends
    BaseModelRepositoryJpa<ScheduleModel> implements ScheduleModelRepository {

  @Inject
  protected ScheduleModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ScheduleModel> type) {
    super(entityManager, type);
  }

  @Override
  public List<ScheduleModel> findByUser(String userId) {
    String query = String.format(
        "select schedule from %s as schedule inner join schedule.tenant as tenant where tenant.userId = :userId",
        type.getName());

    //noinspection unchecked
    return em().createQuery(query).setParameter("userId", userId).getResultList();

  }

  @Override
  public ScheduleModel findByIdAndUser(String id, String user) {
    String query = String.format(
        "select schedule from %s as schedule inner join schedule.tenant as tenant where tenant.userId = :userId and schedule.domainId = :id",
        type.getName());

    //noinspection unchecked
    return (ScheduleModel) JpaResultHelper
        .getSingleResultOrNull(
            em().createQuery(query).setParameter("id", id).setParameter("userId", user));
  }
}
