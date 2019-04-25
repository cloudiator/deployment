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

package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.common.util.concurrent.ListenableFuture;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.concurrent.Phaser;

public interface InstantiationStrategy {

  interface WaitLock {

    void waitFor();
  }

  class WaitLockImpl implements WaitLock {

    private final Phaser phaser;

    public WaitLockImpl(Phaser phaser) {
      this.phaser = phaser;
    }

    @Override
    public void waitFor() {
      phaser.arriveAndAwaitAdvance();
    }
  }

  class CompositeWaitLock implements WaitLock {

    private final List<WaitLock> waitLocks;

    public CompositeWaitLock(
        List<WaitLock> waitLocks) {
      this.waitLocks = waitLocks;
    }

    @Override
    public void waitFor() {
      for (WaitLock waitLock : waitLocks) {
        waitLock.waitFor();
      }
    }
  }

  Instantiation supports();

  WaitLock deployTask(Task task, Schedule schedule,
      List<ListenableFuture<Node>> allocatedResources);

  Schedule instantiate(Schedule schedule) throws InstantiationException;
}
