/*
 * Copyright 2018 University of Ulm
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
import io.github.cloudiator.deployment.domain.Communication;

public class CommunicationDomainRepository {

  private final PortDomainRepository portDomainRepository;
  private final CommunicationModelRepository communicationModelRepository;

  @Inject
  public CommunicationDomainRepository(
      PortDomainRepository portDomainRepository,
      CommunicationModelRepository communicationModelRepository) {
    this.portDomainRepository = portDomainRepository;
    this.communicationModelRepository = communicationModelRepository;
  }

  void save(Communication communication, JobModel job) {
    saveAndGet(communication, job);
  }

  CommunicationModel saveAndGet(Communication communication, JobModel job) {
    final CommunicationModel model = createModel(communication, job);
    communicationModelRepository.save(model);
    return model;
  }

  private CommunicationModel createModel(Communication domain, JobModel job) {

    final PortProvidedModel portProvided = portDomainRepository
        .getPortProvidedModel(domain.source());
    final PortRequiredModel portRequired = portDomainRepository
        .getPortRequiredModel(domain.target());

    return new CommunicationModel(job, portRequired, portProvided);
  }


}
