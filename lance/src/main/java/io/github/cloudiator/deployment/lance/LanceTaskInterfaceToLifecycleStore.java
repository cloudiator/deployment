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

package io.github.cloudiator.deployment.lance;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.lance.container.spec.os.OperatingSystem;
import de.uniulm.omi.cloudiator.lance.lifecycle.LifecycleHandler;
import de.uniulm.omi.cloudiator.lance.lifecycle.LifecycleHandlerType;
import de.uniulm.omi.cloudiator.lance.lifecycle.LifecycleStore;
import de.uniulm.omi.cloudiator.lance.lifecycle.LifecycleStoreBuilder;
import de.uniulm.omi.cloudiator.lance.lifecycle.bash.BashBasedHandlerBuilder;
import de.uniulm.omi.cloudiator.lance.lifecycle.detector.DefaultDetectorFactories;
import io.github.cloudiator.deployment.domain.LanceInterface;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LanceTaskInterfaceToLifecycleStore implements
    Function<LanceInterface, LifecycleStore> {

  private Map<LifecycleHandlerType, String> buildCommandMap(LanceInterface lanceInterface) {
    Map<LifecycleHandlerType, String> commands = Maps.newHashMap();

    if (lanceInterface.init().isPresent()) {
      commands.put(LifecycleHandlerType.INIT, lanceInterface.init().get());
    }

    if (lanceInterface.preInstall().isPresent()) {
      commands.put(LifecycleHandlerType.PRE_INSTALL, lanceInterface.preInstall().get());
    }

    if (lanceInterface.install().isPresent()) {
      commands.put(LifecycleHandlerType.INSTALL, lanceInterface.install().get());
    }
    if (lanceInterface.postInstall().isPresent()) {
      commands.put(LifecycleHandlerType.POST_INSTALL, lanceInterface.postInstall().get());
    }
    if (lanceInterface.preStart().isPresent()) {
      commands.put(LifecycleHandlerType.PRE_START, lanceInterface.preStart().get());
    }
    commands.put(LifecycleHandlerType.START, lanceInterface.start());

    if (lanceInterface.postStart().isPresent()) {
      commands.put(LifecycleHandlerType.POST_START, lanceInterface.postStart().get());
    }
    if (lanceInterface.preStop().isPresent()) {
      commands.put(LifecycleHandlerType.PRE_STOP, lanceInterface.preStop().get());
    }
    if (lanceInterface.stop().isPresent()) {
      commands.put(LifecycleHandlerType.STOP, lanceInterface.stop().get());
    }
    if (lanceInterface.postStop().isPresent()) {
      commands.put(LifecycleHandlerType.POST_STOP, lanceInterface.postStop().get());
    }
    return commands;
  }

  @Override
  public LifecycleStore apply(LanceInterface lanceInterface) {

    Set<OperatingSystem> lanceOperatingSystems = Sets
        .newHashSet(OperatingSystem.UBUNTU_14_04, OperatingSystem.WINDOWS_7);

    final LifecycleStoreBuilder lifecycleStoreBuilder = new LifecycleStoreBuilder();

    for (OperatingSystem lanceOs : lanceOperatingSystems) {
      for (Map.Entry<LifecycleHandlerType, String> entry : buildCommandMap(lanceInterface)
          .entrySet()) {
        final BashBasedHandlerBuilder bashBasedHandlerBuilder =
            new BashBasedHandlerBuilder();
        bashBasedHandlerBuilder.setOperatingSystem(lanceOs);
        bashBasedHandlerBuilder.addCommand(entry.getValue());
        final LifecycleHandler lifecycleHandler =
            bashBasedHandlerBuilder.build(entry.getKey());
        lifecycleStoreBuilder.setHandler(lifecycleHandler, entry.getKey());
      }

      if (lanceInterface.startDetection().isPresent()) {
        final BashBasedHandlerBuilder startHandlerBuilder = new BashBasedHandlerBuilder();
        startHandlerBuilder.addCommand(lanceInterface.startDetection().get());
        startHandlerBuilder.setOperatingSystem(lanceOs);
        lifecycleStoreBuilder.setStartDetector(startHandlerBuilder.buildStartDetector());
      } else {
        lifecycleStoreBuilder
            .setStartDetector(DefaultDetectorFactories.START_DETECTOR_FACTORY.getDefault());
      }
    }

    return lifecycleStoreBuilder.build();
  }
}
