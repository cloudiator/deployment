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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvidedBuilder;
import io.github.cloudiator.deployment.domain.PortRequiredBuilder;
import javax.annotation.Nullable;

class PortModelConverter implements OneWayConverter<PortModel, Port> {

  @Nullable
  @Override
  public Port apply(@Nullable PortModel portModel) {

    if (portModel == null) {
      return null;
    }

    if (portModel instanceof PortRequiredModel) {
      return PortRequiredBuilder.newBuilder().name(portModel.getName())
          .isMandatory(((PortRequiredModel) portModel).getMandatory()).build();
    } else if (portModel instanceof PortProvidedModel) {
      return PortProvidedBuilder.newBuilder().name(portModel.getName())
          .port(((PortProvidedModel) portModel).getPort()).build();
    } else {
      throw new AssertionError("portModel has illegal type " + portModel.getClass());
    }
  }
}
