package io.github.cloudiator.deployment.domain;

import java.util.Set;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public interface CloudiatorClusterProcess extends CloudiatorProcess {

  Set<String> nodes();
}
