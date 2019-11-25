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

import com.google.common.base.MoreObjects;
import java.util.Objects;

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
  public String portProvided() {
    return portProvided;
  }

  @Override
  public String portRequired() {
    return portRequired;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CommunicationImpl that = (CommunicationImpl) o;
    return Objects.equals(portProvided, that.portProvided) &&
        Objects.equals(portRequired, that.portRequired);
  }

  @Override
  public int hashCode() {

    return Objects.hash(portProvided, portRequired);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("portProvided", portProvided)
        .add("portRequired", portRequired)
        .toString();
  }
}
