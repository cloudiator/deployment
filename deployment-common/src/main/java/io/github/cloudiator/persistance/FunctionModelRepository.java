package io.github.cloudiator.persistance;

import java.util.List;

public interface FunctionModelRepository extends ModelRepository<FunctionModel> {

  FunctionModel findByFunctionIdAndTenant(String functionId, String tenant);

  List<FunctionModel> findByTenant(String tenant);

  List<FunctionModel> findByTenantAndCloud(String tenant, String cloudId);

}
