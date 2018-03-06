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
import javax.persistence.EntityManager;

public class PortModelRepositoryJpa extends BaseModelRepositoryJpa<PortModel> implements
    PortModelRepository {

  @Inject
  protected PortModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<PortModel> type) {
    super(entityManager, type);
  }

  @Override
  public PortModel findByName(String name) {

    String query = String.format("from %s where name=:name", type.getName());

    return (PortModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("name", name));
  }

  @Override
  public PortModel findByNameAndUser(String name, String user) {
    String query = String.format(
        "from %s port inner join port.task task inner join task.jobModel job inner join job.tenant tenant where job.name=:name and tenant.userId=:userId",
        type.getName());
    return (PortModel) JpaResultHelper.getSingleResultOrNull(
        em().createQuery(query).setParameter("name", name).setParameter("userId", user));
  }
}
