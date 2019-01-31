/*
 * Copyright 2018 University of Ulm
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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

public class ProcessGroupImpl implements ProcessGroup {

  private final List<CloudiatorProcess> cloudiatorProcesses;
  private final String id;
  private final String userId;
  private final String scheduleId;

  ProcessGroupImpl(String id, String userId, String scheduleId,
      Collection<? extends CloudiatorProcess> cloudiatorProcesses) {
    checkNotNull(id, "id is null");
    checkNotNull(userId, "userId is null");
    checkNotNull(cloudiatorProcesses, "cloudiatorProcesses is null");
    checkNotNull(scheduleId, "scheduleId is null");
    checkProcessOwners(userId, cloudiatorProcesses);
    this.cloudiatorProcesses = ImmutableList.copyOf(cloudiatorProcesses);
    this.id = id;
    this.userId = userId;
    this.scheduleId = scheduleId;
  }

  private Iterable<? extends CloudiatorProcess> checkProcessOwners(String userId,
      Iterable<? extends CloudiatorProcess> cloudiatorProcesses) {

    for (CloudiatorProcess cloudiatorProcess : cloudiatorProcesses) {
      checkArgument(cloudiatorProcess.userId().equals(userId), String
          .format("UserId of process %s is not equal to id of group %s.",
              cloudiatorProcess.userId(), userId));
    }

    return cloudiatorProcesses;

  }


  @Override
  public String userId() {
    return userId;
  }

  @Override
  public String scheduleId() {
    return this.scheduleId;
  }

  @Override
  public List<CloudiatorProcess> cloudiatorProcesses() {
    return cloudiatorProcesses;
  }

  @Override
  public String id() {
    return id;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("userId", userId)
        .add("cloudiatorProcesses", Joiner.on(",").join(cloudiatorProcesses))
        .toString();
  }

}

