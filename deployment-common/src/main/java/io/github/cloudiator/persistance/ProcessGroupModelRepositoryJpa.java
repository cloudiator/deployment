package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */

public class ProcessGroupModelRepositoryJpa extends BaseModelRepositoryJpa<ProcessGroupModel> implements
    ProcessGroupModelRepository {

  @Inject
  protected ProcessGroupModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ProcessGroupModel> type) {
    super(entityManager, type);
  }

  @Override
  public ProcessGroupModel findByTenantAndDomainId(String userId, String domainId) {
    checkNotNull(userId, "userId is null");
    checkNotNull(domainId, "domainId is null");
    String queryString = String.format(
        "select processGroup from %s processGroup inner join processGroup.scheduleModel schedule inner join schedule.tenantModel tenant where tenant.userId = :userId and processGroup.domainId = :domainId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId)
        .setParameter("domainId", domainId);
    @SuppressWarnings("unchecked") List<ProcessGroupModel> processGroups = query.getResultList();
    return processGroups.stream().findFirst().orElse(null);
  }

  @Override
  public List<ProcessGroupModel> findByTenant(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String
        .format(
            "select processGroup from %s processGroup inner join processGroup.scheduleModel schedule inner join schedule.tenantModel tenant where tenant.userId = :userId",
            type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    //noinspection unchecked
    return query.getResultList();
  }
}