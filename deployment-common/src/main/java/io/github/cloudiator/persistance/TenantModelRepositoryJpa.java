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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 31.05.17.
 */
class TenantModelRepositoryJpa extends BaseModelRepositoryJpa<TenantModel> implements
    TenantModelRepository {

  @Inject
  TenantModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<TenantModel> type) {
    super(entityManager, type);
  }

  @Override
  public TenantModel findByUserId(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String
        .format("from %s where userId=:userId", type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    try {
      //noinspection unchecked
      return (TenantModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public synchronized TenantModel createOrGet(String userId) {
    checkNotNull(userId, "userId is null");
    final TenantModel byUserId = findByUserId(userId);
    if (byUserId != null) {
      return byUserId;
    }
    TenantModel tenant = new TenantModel(userId);
    return save(tenant);
  }
}
