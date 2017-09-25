package io.github.cloudiator.deployment.persistance.repositories;

import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.deployment.persistance.entities.TenantModel;

/**
 * Created by daniel on 31.05.17.
 */
public interface TenantModelRepository extends ModelRepository<TenantModel> {

  TenantModel findByUserId(String userId);

  TenantModel createOrGet(String userId);

}
