package io.github.cloudiator.deployment.persistance.converters;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvidedBuilder;
import io.github.cloudiator.deployment.domain.PortRequiredBuilder;
import io.github.cloudiator.deployment.persistance.entities.PortModel;
import io.github.cloudiator.deployment.persistance.entities.PortProvidedModel;
import io.github.cloudiator.deployment.persistance.entities.PortRequiredModel;
import javax.annotation.Nullable;

public class PortModelConverter implements OneWayConverter<PortModel, Port> {

  @Nullable
  @Override
  public Port apply(@Nullable PortModel portModel) {

    if (portModel == null) {
      return null;
    }

    if (portModel instanceof PortRequiredModel) {
      return PortRequiredBuilder.newBuilder().name(portModel.getName())
          .isMandatory(((PortRequiredModel) portModel).getMandatory())
          .updateAction(((PortRequiredModel) portModel).getUpdateAction()).build();
    } else if (portModel instanceof PortProvidedModel) {
      return PortProvidedBuilder.newBuilder().name(portModel.getName())
          .port(((PortProvidedModel) portModel).getPort()).build();
    } else {
      throw new AssertionError("portModel has illegal type " + portModel.getClass());
    }
  }
}
