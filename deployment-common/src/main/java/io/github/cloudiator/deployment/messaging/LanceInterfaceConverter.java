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

package io.github.cloudiator.deployment.messaging;

import static com.google.common.base.Strings.isNullOrEmpty;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.LanceInterface;
import io.github.cloudiator.deployment.domain.LanceInterfaceBuilder;
import org.cloudiator.messages.entities.TaskEntities;
import org.cloudiator.messages.entities.TaskEntities.LanceInterface.Builder;

public class LanceInterfaceConverter implements
    TwoWayConverter<TaskEntities.LanceInterface, LanceInterface> {

  public static final LanceInterfaceConverter INSTANCE = new LanceInterfaceConverter();

  private LanceInterfaceConverter() {

  }

  @Override
  public TaskEntities.LanceInterface applyBack(LanceInterface lanceInterface) {

    final Builder builder = TaskEntities.LanceInterface.newBuilder();

    if (lanceInterface.init().isPresent()) {
      builder.setInit(lanceInterface.init().get());
    }
    if (lanceInterface.install().isPresent()) {
      builder.setInstall(lanceInterface.install().get());
    }
    if (lanceInterface.postInstall().isPresent()) {
      builder.setPostInstall(lanceInterface.postInstall().get());
    }
    if (lanceInterface.postStart().isPresent()) {
      builder.setPostStart(lanceInterface.postStart().get());
    }
    if (lanceInterface.postStop().isPresent()) {
      builder.setPostStop(lanceInterface.postStop().get());
    }
    if (lanceInterface.preInstall().isPresent()) {
      builder.setPreInstall(lanceInterface.preInstall().get());
    }
    if (lanceInterface.preStart().isPresent()) {
      builder.setPreStart(lanceInterface.preStart().get());
    }
    if (lanceInterface.preStop().isPresent()) {
      builder.setPreStop(lanceInterface.preStop().get());
    }
    if (lanceInterface.shutdown().isPresent()) {
      builder.setShutdown(lanceInterface.shutdown().get());
    }
    if (lanceInterface.startDetection().isPresent()) {
      builder.setStartDetection(lanceInterface.startDetection().get());
    }
    if (lanceInterface.stop().isPresent()) {
      builder.setStop(lanceInterface.stop().get());
    }
    if (lanceInterface.stopDetection().isPresent()) {
      builder.setStopDetection(lanceInterface.stopDetection().get());
    }
    builder.setStart(lanceInterface.start());

    return builder.build();
  }

  @Override
  public LanceInterface apply(TaskEntities.LanceInterface lanceInterface) {

    final LanceInterfaceBuilder lanceInterfaceBuilder = LanceInterfaceBuilder.newBuilder();

    if (!isNullOrEmpty(lanceInterface.getInit())) {
      lanceInterfaceBuilder.init(lanceInterface.getInit());
    }
    if (!isNullOrEmpty(lanceInterface.getInstall())) {
      lanceInterfaceBuilder.install(lanceInterface.getInstall());
    }
    if (!isNullOrEmpty(lanceInterface.getPostInstall())) {
      lanceInterfaceBuilder.postInstall(lanceInterface.getPostInstall());
    }
    if (!isNullOrEmpty(lanceInterface.getPostStart())) {
      lanceInterfaceBuilder.postStart(lanceInterface.getPostStart());
    }
    if (!isNullOrEmpty(lanceInterface.getPostStop())) {
      lanceInterfaceBuilder.postStop(lanceInterface.getPostStop());
    }
    if (!isNullOrEmpty(lanceInterface.getPreInstall())) {
      lanceInterfaceBuilder.preInstall(lanceInterface.getPreInstall());
    }
    if (!isNullOrEmpty(lanceInterface.getPreStart())) {
      lanceInterfaceBuilder.preStart(lanceInterface.getPreStart());
    }
    if (!isNullOrEmpty(lanceInterface.getPreStop())) {
      lanceInterfaceBuilder.preStop(lanceInterface.getPreStop());
    }
    if (!isNullOrEmpty(lanceInterface.getShutdown())) {
      lanceInterfaceBuilder.shutdown(lanceInterface.getShutdown());
    }
    if (!isNullOrEmpty(lanceInterface.getStartDetection())) {
      lanceInterfaceBuilder.startDetection(lanceInterface.getStartDetection());
    }
    if (!isNullOrEmpty(lanceInterface.getStop())) {
      lanceInterfaceBuilder.stop(lanceInterface.getStop());
    }
    if (!isNullOrEmpty(lanceInterface.getStopDetection())) {
      lanceInterfaceBuilder.stopDetection(lanceInterface.getStopDetection());
    }

    lanceInterfaceBuilder.start(lanceInterface.getStart());

    return lanceInterfaceBuilder.build();
  }
}
