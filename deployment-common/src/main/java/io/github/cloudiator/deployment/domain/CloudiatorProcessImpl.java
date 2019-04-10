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

import com.google.common.base.MoreObjects;
import java.util.Optional;
import javax.annotation.Nullable;

abstract class CloudiatorProcessImpl implements CloudiatorProcess {

  protected final String id;
  @Nullable
  private final String originId;
  protected final String userId;
  protected final String scheduleId;
  protected final String taskName;
  protected final String taskInterface;
  protected final CloudiatorProcess.ProcessState state;
  protected final Type type;
  @Nullable
  private final String diagnostic;
  @Nullable
  private final String reason;

  CloudiatorProcessImpl(String id, @Nullable String originId, String userId, String scheduleId,
      String taskName, String taskInterface,
      CloudiatorProcess.ProcessState state, Type type, @Nullable String diagnostic,
      @Nullable String reason) {

    checkNotNull(id, "id is null");
    checkArgument(!id.isEmpty(), "id is empty");
    this.id = id;

    this.originId = originId;

    checkNotNull(userId, "userId is null");
    checkArgument(!userId.isEmpty(), "userId is empty");
    this.userId = userId;

    checkNotNull(scheduleId, "scheduleId is null");
    checkArgument(!scheduleId.isEmpty(), "scheduleId is empty");
    this.scheduleId = scheduleId;

    checkNotNull(taskName, "taskName is null");
    checkArgument(!taskName.isEmpty(), "taskName is empty");
    this.taskName = taskName;

    checkNotNull(taskInterface, "taskInterface is null");
    checkArgument(!taskInterface.isEmpty(), "taskInterface is empty");
    this.taskInterface = taskInterface;

    checkNotNull(state, "state is null");
    this.state = state;

    checkNotNull(type, "type is null");
    this.type = type;

    this.diagnostic = diagnostic;
    this.reason = reason;
  }

  @Override
  public Optional<String> originId() {
    return Optional.ofNullable(originId);
  }

  @Override
  public String scheduleId() {
    return scheduleId;
  }

  @Override
  public String taskId() {
    return taskName;
  }

  @Override
  public String taskInterface() {
    return taskInterface;
  }

  @Override
  public CloudiatorProcess.ProcessState state() {
    return state;
  }


  @Override
  public Type type() {
    return type;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public Optional<String> diagnostic() {
    return Optional.ofNullable(diagnostic);
  }

  @Override
  public Optional<String> reason() {
    return Optional.ofNullable(reason);
  }

  protected MoreObjects.ToStringHelper stringHelper() {
    return MoreObjects.toStringHelper(this).add("id", id).add("originId", originId)
        .add("userId", userId)
        .add("scheduleId", scheduleId)
        .add("taskName", taskName)
        .add("taskInterface", taskInterface)
        .add("state", state).add("type", type)
        .add("diagnostic", diagnostic).add("reason", reason);
  }

  @Override
  public String toString() {
    return stringHelper().toString();
  }
}
