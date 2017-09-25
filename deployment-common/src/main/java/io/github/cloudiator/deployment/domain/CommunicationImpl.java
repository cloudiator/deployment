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

package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

class CommunicationImpl implements Communication {

  private final String portProvided;
  private final String portRequired;

  CommunicationImpl(String portProvided,
      String portRequired) {

    checkNotNull(portProvided, "portProvided is null");
    checkNotNull(portRequired, "portRequired is null");

    this.portProvided = portProvided;
    this.portRequired = portRequired;
  }

  @Override
  public String target() {
    return portProvided;
  }

  @Override
  public String source() {
    return portRequired;
  }
}
