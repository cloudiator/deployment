package io.github.cloudiator.deployment.domain;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */
public class ProcessGroups {

  private ProcessGroups() {
    throw new AssertionError("Do not instantiate");
  }

  private static String generateId() {
    return UUID.randomUUID().toString();
  }

  public static ProcessGroup of(Collection<CloudiatorProcess> nodes) {
    return new ProcessGroupImpl(generateId(), nodes);
  }

  public static ProcessGroup of(String id, Collection<CloudiatorProcess> nodes) {
    return new ProcessGroupImpl(id, nodes);
  }

  public static ProcessGroup ofSingle(CloudiatorProcess cloudiatorProcess) {
    return new ProcessGroupImpl(generateId(), cloudiatorProcess);
  }

  public static ProcessGroup ofSingle(String id, CloudiatorProcess cloudiatorProcess) {
    return new ProcessGroupImpl(id, cloudiatorProcess);
  }

}
