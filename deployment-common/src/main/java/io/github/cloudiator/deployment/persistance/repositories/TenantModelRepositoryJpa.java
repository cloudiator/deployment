package io.github.cloudiator.deployment.persistance.repositories;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.deployment.persistance.entities.TenantModel;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 31.05.17.
 */
public class TenantModelRepositoryJpa extends BaseModelRepositoryJpa<TenantModel> implements
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
  public TenantModel createOrGet(String userId) {
    checkNotNull(userId, "userId is null");
    final TenantModel byUserId = findByUserId(userId);
    if (byUserId != null) {
      return byUserId;
    }
    TenantModel tenant = new TenantModel(userId);
    save(tenant);
    return tenant;
  }
}
