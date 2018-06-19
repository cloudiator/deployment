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

import java.util.Optional;
import javax.annotation.Nullable;

class PortRequiredImpl extends PortImpl implements PortRequired {

  @Nullable
  private final String updateAction;
  private final boolean isMandatory;

  PortRequiredImpl(String name, @Nullable String updateAction, boolean isMandatory) {
    super(name);

    if (updateAction != null) {
      checkArgument(!updateAction.isEmpty(), "updateAction is empty");
    }

    this.updateAction = updateAction;
    this.isMandatory = isMandatory;
  }

  @Override
  public Optional<String> updateAction() {
    return Optional.ofNullable(updateAction);
  }

  @Override
  public boolean isMandatory() {
    return isMandatory;
  }
}
