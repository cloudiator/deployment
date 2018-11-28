package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionModelRepositoryJpa extends BaseModelRepositoryJpa<FunctionModel>
    implements FunctionModelRepository {

  @Inject
  protected FunctionModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<FunctionModel> type) {
    super(entityManager, type);
  }

  @Override
  public FunctionModel findByFunctionIdAndTenant(String functionId, String tenant) {
    checkNotNull(functionId, "functionId is null");
    checkNotNull(tenant, "tenant is null");
    String queryString = String.format(
        "select func from %s func inner join func.tenantModel tenant where tenant.userId=:tenant and func.functionId = :id",
        type.getName());

    Query query = em().createQuery(queryString)
        .setParameter("id", functionId)
        .setParameter("tenant", tenant);
    try {
      return (FunctionModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<FunctionModel> findByTenant(String tenant) {
    checkNotNull(tenant, "tenant is null");
    String queryString = String.format(
        "select func from %s func inner join func.tenantModel tenant where tenant.userId=:tenant",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant);
    //noinspection unchecked
    return (List<FunctionModel>) query.getResultList();
  }

  @Override
  public List<FunctionModel> findByTenantAndCloud(String tenant, String cloudId) {
    checkNotNull(tenant, "tenant is null");
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String.format(
        "select func from %s func inner inner join func.tenantModel tenant where tenant.userId=:tenant and func.cloudId = :cloudId",
        type.getName());
    Query query = em().createQuery(queryString)
        .setParameter("tenant", tenant)
        .setParameter("cloudId", cloudId);
    //noinspection unchecked
    return (List<FunctionModel>) query.getResultList();
  }
}
