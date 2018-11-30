package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Function;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

public class FunctionDomainRepository {
  private final FunctionModelRepository functionModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private final FunctionModelConverter functionModelConverter;

  @Inject
  public FunctionDomainRepository(
      FunctionModelRepository functionModelRepository,
      TenantModelRepository tenantModelRepository,
      FunctionModelConverter functionModelConverter) {
    this.functionModelRepository = functionModelRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.functionModelConverter = functionModelConverter;
  }

  public Function findByIdAndTenant(String id, String tenant) {
    return functionModelConverter.apply(
        functionModelRepository.findByFunctionIdAndTenant(id, tenant));
  }

  public List<Function> findAll(String userId) {
    return functionModelRepository.findByTenant(userId).stream()
        .map(functionModelConverter).collect(Collectors.toList());
  }

  public void save(Function function, String userId) {
    saveAndGet(function, userId);
  }

  public void delete(String functionId, String userId) {
    FunctionModel function = functionModelRepository.findByFunctionIdAndTenant(functionId, userId);
    checkState(function != null, String.format("Function with id %s doesn't exists", functionId));
    functionModelRepository.delete(function);
  }

  FunctionModel saveAndGet(Function function, String userId) {
    FunctionModel functionModel = functionModelRepository.findByFunctionIdAndTenant(function.id(), userId);
    if (functionModel == null) {
      functionModel = createModel(function, userId);
    } else {
      functionModel = updateModel(function, functionModel);
    }
    functionModelRepository.save(functionModel);
    return functionModel;
  }

  private FunctionModel createModel(Function function, String userId) {
    final TenantModel tenantModel = tenantModelRepository.createOrGet(userId);
    return new FunctionModel(
        function.id(),
        tenantModel,
        function.cloudId(),
        function.locationId(),
        function.memory(),
        function.runtime(),
        function.stackId());
  }

  private FunctionModel updateModel(Function function, FunctionModel functionModel) {
    return functionModel.setStackId(function.stackId());
  }
}
