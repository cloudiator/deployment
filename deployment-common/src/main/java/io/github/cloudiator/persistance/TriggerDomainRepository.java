package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.HttpTrigger;
import io.github.cloudiator.deployment.domain.Trigger;

public class TriggerDomainRepository {

  private final TriggerModelRepository triggerModelRepository;

  @Inject
  public TriggerDomainRepository(TriggerModelRepository triggerModelRepository) {
    this.triggerModelRepository = triggerModelRepository;
  }

  TriggerModel saveAndGet(Trigger domain, FaasTaskInterfaceModel faasInterface) {
    final TriggerModel triggerModel = createTriggerModel(domain, faasInterface);
    triggerModelRepository.save(triggerModel);
    return triggerModel;
  }

  private TriggerModel createTriggerModel(Trigger domain,
      FaasTaskInterfaceModel faasInterface) {
    if (domain instanceof HttpTrigger) {
      return createHttpTriggerModel((HttpTrigger) domain, faasInterface);
    } else {
      throw new AssertionError("Trigger is of unknown type " + domain.getClass().getName());
    }
  }

  private TriggerModel createHttpTriggerModel(HttpTrigger domain, FaasTaskInterfaceModel faasInterface) {
    return new HttpTriggerModel(faasInterface,
        domain.httpMethod(),
        domain.httpPath());
  }
}
