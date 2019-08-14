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
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskInterface;
import io.github.cloudiator.deployment.scheduler.instantiation.DependencyGraph.Dependencies;
import io.github.cloudiator.domain.Node;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

public interface InstantiationStrategy {

  class WaitLockImpl<T> implements Future<T> {

    private final CountDownLatch countDownLatch;
    private final T t;

    public WaitLockImpl(CountDownLatch countDownLatch, T t) {
      this.countDownLatch = countDownLatch;
      this.t = t;
    }

    @Override
    public boolean cancel(boolean b) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return countDownLatch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException {
      countDownLatch.await();
      return t;
    }

    @Override
    public T get(long l, TimeUnit timeUnit)
        throws InterruptedException, TimeoutException {
      final boolean await = countDownLatch.await(l, timeUnit);
      if (!await) {
        throw new TimeoutException();
      }
      return t;
    }
  }

  Instantiation supports();

  Future<Collection<CloudiatorProcess>> deployTask(Task task, TaskInterface taskInterface,
      Schedule schedule,
      Collection<ListenableFuture<Node>> allocatedResources,
      @Nullable Dependencies dependencies);

  Schedule instantiate(Schedule schedule) throws InstantiationException;
}
