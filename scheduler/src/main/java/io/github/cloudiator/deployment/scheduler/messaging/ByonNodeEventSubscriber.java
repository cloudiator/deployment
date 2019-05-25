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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.inject.Inject;
import org.cloudiator.messages.Byon;
import org.cloudiator.messaging.services.ByonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeEventSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ByonNodeEventSubscriber.class);
  private final ByonService byonService;

  @Inject
  public ByonNodeEventSubscriber(ByonService byonService) {
    this.byonService = byonService;
  }

  @Override
  public void run() {
    byonService.subscribeByonNodeEvents((id, content) -> {

      Byon.ByonNode byonNode = content.getByonNode();
    });

  }
}
