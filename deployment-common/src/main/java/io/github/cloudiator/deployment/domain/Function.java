package io.github.cloudiator.deployment.domain;

import de.uniulm.omi.cloudiator.domain.Identifiable;
import io.github.cloudiator.domain.Runtime;

public interface Function extends Identifiable {

  String cloudId();

  String locationId();

  int memory();

  Runtime runtime();

  String stackId();

}
