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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;

class PortProvidedImpl extends PortImpl implements PortProvided {

  private final int port;

  PortProvidedImpl(String name, int port) {
    super(name);

    checkArgument(!(port < 0) && !(port > 65535));
    this.port = port;
  }

  @Override
  public int port() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    PortProvidedImpl that = (PortProvidedImpl) o;
    return port == that.port;
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), port);
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper().add("port", port);
  }
}
