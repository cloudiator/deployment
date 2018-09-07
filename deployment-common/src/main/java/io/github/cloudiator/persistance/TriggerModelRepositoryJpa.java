package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import javax.persistence.EntityManager;

public class TriggerModelRepositoryJpa extends BaseModelRepositoryJpa<TriggerModel>
    implements TriggerModelRepository {

  @Inject
  protected TriggerModelRepositoryJpa(
      Provider<EntityManager> entityManager, TypeLiteral<TriggerModel> type) {
    super(entityManager, type);
  }
}
