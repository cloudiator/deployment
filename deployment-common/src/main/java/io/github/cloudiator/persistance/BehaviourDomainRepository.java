/*
 * Copyright 2014-2019 University of Ulm
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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Behaviour;
import io.github.cloudiator.deployment.domain.Interval;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import io.github.cloudiator.deployment.domain.ServiceBehaviour;

public class BehaviourDomainRepository {

  private final BehaviourModelRepository behaviourModelRepository;
  private final IntervalModelRepository intervalModelRepository;

  @Inject
  public BehaviourDomainRepository(
      BehaviourModelRepository behaviourModelRepository,
      IntervalModelRepository intervalModelRepository) {
    this.behaviourModelRepository = behaviourModelRepository;
    this.intervalModelRepository = intervalModelRepository;
  }

  BehaviourModel saveAndGet(Behaviour domain) {

    final BehaviourModel behaviourModel = createBehaviourModel(domain);
    behaviourModelRepository.save(behaviourModel);
    return behaviourModel;

  }

  private BehaviourModel createBehaviourModel(Behaviour domain) {

    if (domain instanceof ServiceBehaviour) {
      return createServiceBehaviourModel((ServiceBehaviour) domain);
    } else if (domain instanceof PeriodicBehaviour) {
      return createPeriodicBehaviourModel((PeriodicBehaviour) domain);
    } else {
      throw new AssertionError("TaskBehaviour is of unknown type " + domain.getClass().getName());
    }
  }

  private IntervalModel createInterval(Interval domain) {
    return intervalModelRepository.save(new IntervalModel(domain.timeUnit(), domain.period()));
  }

  private PeriodicBehaviourModel createPeriodicBehaviourModel(PeriodicBehaviour domain) {
    return new PeriodicBehaviourModel(createInterval(domain.interval()),
        domain.collisionHandling());
  }

  private ServiceBehaviourModel createServiceBehaviourModel(ServiceBehaviour domain) {
    return new ServiceBehaviourModel(domain.restart());
  }
}
