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

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;

class PortRequiredImpl extends PortImpl implements PortRequired {

  private final boolean isMandatory;

  PortRequiredImpl(String name, boolean isMandatory) {
    super(name);

    this.isMandatory = isMandatory;
  }

  @Override
  public boolean isMandatory() {
    return isMandatory;
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
    PortRequiredImpl that = (PortRequiredImpl) o;
    return isMandatory == that.isMandatory;
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), isMandatory);
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper().add("isMandatory", isMandatory);
  }
}
