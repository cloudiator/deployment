/*
 * Copyright 2014-2018 University of Ulm
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

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Singleton
public class ScheduleRequestQueue implements BlockingQueue<ScheduleRequest> {

  private final LinkedBlockingQueue<ScheduleRequest> delegate;

  public ScheduleRequestQueue() {
    delegate = new LinkedBlockingQueue<ScheduleRequest>();
  }

  @Override
  public boolean add(ScheduleRequest scheduleRequest) {
    return delegate.add(scheduleRequest);
  }

  @Override
  public boolean offer(ScheduleRequest scheduleRequest) {
    return delegate.offer(scheduleRequest);
  }

  @Override
  public ScheduleRequest remove() {
    return delegate.remove();
  }

  @Override
  public ScheduleRequest poll() {
    return delegate.poll();
  }

  @Override
  public ScheduleRequest element() {
    return delegate.element();
  }

  @Override
  public ScheduleRequest peek() {
    return delegate.peek();
  }

  @Override
  public void put(ScheduleRequest scheduleRequest) throws InterruptedException {
    delegate.put(scheduleRequest);
  }

  @Override
  public boolean offer(ScheduleRequest scheduleRequest, long l, TimeUnit timeUnit)
      throws InterruptedException {
    return delegate.offer(scheduleRequest, l, timeUnit);
  }

  @Override
  public ScheduleRequest take() throws InterruptedException {
    return delegate.take();
  }

  @Override
  public ScheduleRequest poll(long l, TimeUnit timeUnit) throws InterruptedException {
    return delegate.poll(l, timeUnit);
  }

  @Override
  public int remainingCapacity() {
    return delegate.remainingCapacity();
  }

  @Override
  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return delegate.containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends ScheduleRequest> collection) {
    return delegate.addAll(collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    return delegate.removeAll(collection);
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    return delegate.retainAll(collection);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override
  public Iterator<ScheduleRequest> iterator() {
    return delegate.iterator();
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return delegate.toArray(ts);
  }

  @Override
  public int drainTo(Collection<? super ScheduleRequest> collection) {
    return delegate.drainTo(collection);
  }

  @Override
  public int drainTo(Collection<? super ScheduleRequest> collection, int i) {
    return delegate.drainTo(collection, i);
  }
}
