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

import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import java.time.Duration;
import javax.annotation.Nullable;

public class LanceAgentContext {

  private final Config config;

  public LanceAgentContext() {
    this(Configuration.conf());
  }

  public LanceAgentContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "lance");
  }

  @Nullable
  public Duration waitTimeout() {
    try {
      return config.getDuration(LanceAgentConstants.WAIT_TIMEOUT);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("config", config).toString();
  }

}
