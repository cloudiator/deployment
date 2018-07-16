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

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import java.util.Set;
import java.util.stream.Collectors;

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
  public Set<Communication> attachedCommunications(PortProvided providedPort) {

    return communications().stream().filter(
        communication -> communication.portProvided().equals(providedPort.name()))
        .collect(Collectors.toSet());
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
