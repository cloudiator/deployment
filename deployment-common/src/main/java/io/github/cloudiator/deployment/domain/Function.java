package io.github.cloudiator.deployment.domain;

import de.uniulm.omi.cloudiator.domain.Identifiable;

public interface Function extends Identifiable {

  String cloudId();

  String locationId();

  int memory();

  String runtime();

  String stackId();

}
