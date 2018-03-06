/*
 * Copyright 2017 University of Ulm
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

package io.github.cloudiator.deployment.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.CommunicationBuilder;
import org.cloudiator.messages.entities.JobEntities;

public class CommunicationConverter implements
    TwoWayConverter<JobEntities.Communication, Communication> {

  @Override
  public JobEntities.Communication applyBack(Communication communication) {
    return JobEntities.Communication.newBuilder()
        .setPortProvided(communication.target())
        .setPortRequired(communication.source()).build();
  }

  @Override
  public Communication apply(JobEntities.Communication communication) {
    return CommunicationBuilder.newBuilder().portProvided(communication.getPortProvided())
        .portRequired(communication.getPortRequired()).build();
  }
}
