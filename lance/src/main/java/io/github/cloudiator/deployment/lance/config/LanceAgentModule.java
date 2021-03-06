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

package io.github.cloudiator.deployment.lance.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import java.time.Duration;

public class LanceAgentModule extends AbstractModule {

  private final LanceAgentContext lanceAgentContext;

  public LanceAgentModule(
      LanceAgentContext lanceAgentContext) {
    this.lanceAgentContext = lanceAgentContext;
  }

  @Override
  protected void configure() {

    if (lanceAgentContext.waitTimeout() != null) {
      bind(Duration.class).annotatedWith(Names.named(LanceAgentConstants.WAIT_TIMEOUT))
          .toInstance(lanceAgentContext.waitTimeout());
    }
  }
}
