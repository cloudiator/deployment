package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.CommunicationBuilder;
import javax.annotation.Nullable;

class CommunicationModelConverter implements
    OneWayConverter<CommunicationModel, Communication> {

  static CommunicationModelConverter INSTANCE = new CommunicationModelConverter();

  private CommunicationModelConverter() {

  }

  @Nullable
  @Override
  public Communication apply(@Nullable CommunicationModel communicationModel) {

    if (communicationModel == null) {
      return null;
    }

    return CommunicationBuilder.newBuilder()
        .portProvided(communicationModel.getProvidedPort().getName())
        .portRequired(communicationModel.getRequiredPort().getName()).build();
  }
}
