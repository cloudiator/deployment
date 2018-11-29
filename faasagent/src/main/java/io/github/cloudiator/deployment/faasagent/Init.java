package io.github.cloudiator.deployment.faasagent;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;

public class Init {

  private final PersistService persistService;

  @Inject
  Init(PersistService persistService) {
    this.persistService = persistService;
    init();
  }

  private void init() {
    persistService.start();
  }
}
