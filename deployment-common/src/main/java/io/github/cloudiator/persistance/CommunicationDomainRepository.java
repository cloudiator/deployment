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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.deployment.domain.Communication;
import java.util.function.Predicate;
import javax.annotation.Nullable;

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

  @Nullable
  private PortModel findPortinJobModel(JobModel jobModel, String port) {
    return jobModel.getTasks().stream().flatMap(taskModel -> taskModel.getPorts().stream()).filter(
        new Predicate<PortModel>() {
          @Override
          public boolean test(PortModel portModel) {
            return portModel.getName().equals(port);
          }
        }).collect(StreamUtil.getOnly()).orElse(null);
  }

  private CommunicationModel createModel(Communication domain, JobModel job) {

    final PortModel providedPort = findPortinJobModel(job, domain.portProvided());
    final PortModel requiredPort = findPortinJobModel(job, domain.portRequired());

    checkNotNull(providedPort,
        String.format("No task in job %s provides port %s", job.getName(), domain.portProvided()));
    checkState(providedPort instanceof PortProvidedModel,
        String.format("Expected port %s to be of type PortProvidedModel but was %s",
            providedPort.getName(), providedPort.getClass().getName()));

    checkNotNull(requiredPort,
        String.format("No task in job %s requires port %s", job.getName(), domain.portRequired()));
    checkState(requiredPort instanceof PortRequiredModel,
        String.format("Expected port %s to be of type PortRequiredModel but was %s",
            requiredPort.getName(), requiredPort.getClass().getName()));

    return new CommunicationModel(job, (PortRequiredModel) requiredPort,
        (PortProvidedModel) providedPort);
  }


}
