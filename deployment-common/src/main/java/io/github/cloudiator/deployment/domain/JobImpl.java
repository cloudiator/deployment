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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.deployment.graph.Graphs;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by daniel on 13.02.17.
 */
class JobImpl extends JobNewImpl implements Job {

  private final String id;


  JobImpl(String id, String name, Set<Task> tasks,
      Set<Communication> communications) {
    super(name, tasks, communications);

    checkNotNull(id, "id is null");
    checkArgument(!id.isEmpty(), "id is empty");

    this.id = id;
    validateCommunication();
  }

  private boolean validateCommunication() {
    //todo
    return true;
  }


  @Override
  public Task providingTask(Communication communication) {
    checkArgument(communications().contains(communication),
        String.format("Job does not contain communication %s.", communication));

    return this.tasks().stream().filter(task -> task.providedPorts().stream().map(Port::name)
        .collect(Collectors.toSet())
        .contains(communication.portProvided())).collect(StreamUtil.getOnly())
        .orElseThrow(() -> new IllegalStateException(
            String.format(
                "Communication %s references provided port %s but Job %s contains no task providing this port",
                communication, communication.portProvided(), this)));
  }

  @Override
  public Task requiredTask(Communication communication) {
    checkArgument(communications().contains(communication),
        String.format("Job does not contain communication %s.", communication));

    return this.tasks().stream().filter(task -> task.requiredPorts().stream().map(Port::name)
        .collect(Collectors.toSet())
        .contains(communication.portRequired())).collect(StreamUtil.getOnly())
        .orElseThrow(() -> new IllegalStateException(
            String.format(
                "Communication %s references required port %s but Job %s contains no task requiring this port",
                communication, communication.portRequired(), this)));
  }

  @Override
  public Set<Task> consumedBy(Task task) {

    Set<Task> targets = new HashSet<>();
    for (PortProvided provided : task.providedPorts()) {
      for (Communication communication : attachedCommunications(provided)) {
        targets.add(requiredTask(communication));
      }
    }

    return targets;
  }

  @Override
  public PortRequired requiredPort(Communication communication) {
    checkArgument(communications().contains(communication),
        String.format("Job does not contain communication %s.", communication));

    return this.tasks().stream().flatMap(
        (Function<Task, Stream<PortRequired>>) task -> task.requiredPorts().stream())
        .filter(portRequired -> portRequired.name().equals(communication.portRequired()))
        .collect(StreamUtil.getOnly()).orElseThrow(() -> new IllegalStateException(String.format(
            "Communication %s references required port %s but no task of Job %s has this port attached.",
            communication, communication.portRequired(), this)));
  }

  @Override
  public PortProvided providedPort(Communication communication) {
    checkArgument(communications().contains(communication),
        String.format("Job does not contain communication %s.", communication));

    return this.tasks().stream().flatMap(
        (Function<Task, Stream<PortProvided>>) task -> task.providedPorts().stream())
        .filter(portProvided -> portProvided.name().equals(communication.portProvided()))
        .collect(StreamUtil.getOnly()).orElseThrow(() -> new IllegalStateException(String.format(
            "Communication %s references provided port %s but no task of Job %s has this port attached.",
            communication, communication.portProvided(), this)));

  }

  @Override
  public PortProvided getProvidingPort(PortRequired portRequired) {

    checkNotNull(portRequired, "portRequired is null");

    final Set<Communication> communications = attachedCommunications(portRequired);

    if (communications.isEmpty()) {
      throw new IllegalStateException(String
          .format("Required port with name %s is not mapped to any communication",
              portRequired.name()));
    }

    if (communications.size() > 1) {
      throw new IllegalStateException(String
          .format("Required port with name %s is not mapped to multiple communications %s",
              portRequired.name(), Joiner.on(",").join(communications)));
    }

    final Communication communication = communications.stream().findFirst().get();
    final Task providingTask = providingTask(communication);

    final Optional<PortProvided> port = providingTask
        .findPort(communication.portProvided(), PortProvided.class);

    return port.orElseThrow(() -> new IllegalStateException(
        "Could not find a providing port for required port " + portRequired));
  }

  @Override
  public Set<Communication> attachedCommunications(Port port) {

    return communications().stream().filter(
        communication -> communication.portProvided().equals(port.name()) || communication
            .portRequired()
            .equals(port.name())).collect(Collectors.toSet());
  }

  @Override
  public Iterator<Task> tasksInOrder() {
    return Graphs.jobGraph(this).evaluationOrder();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("id", id);
  }
}
