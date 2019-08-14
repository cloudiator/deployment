package io.github.cloudiator.persistance;

import java.util.List;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */
@Deprecated
public interface ProcessGroupModelRepository extends ModelRepository<ProcessGroupModel>  {

  ProcessGroupModel findByTenantAndDomainId(String userId, String domainId);

  List<ProcessGroupModel> findByTenant(String userId);

}
