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
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Singleton
public class ProcessRequestQueue implements BlockingQueue<ProcessRequest> {

  private final LinkedBlockingQueue<ProcessRequest> delegate;

  public ProcessRequestQueue() {
    delegate = new LinkedBlockingQueue<>();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public int remainingCapacity() {
    return delegate.remainingCapacity();
  }

  @Override
  public void put(ProcessRequest processRequest) throws InterruptedException {
    delegate.put(processRequest);
  }

  @Override
  public boolean offer(
      ProcessRequest processRequest, long l, TimeUnit timeUnit) throws InterruptedException {
    return delegate.offer(processRequest, l, timeUnit);
  }

  @Override
  public boolean offer(
      ProcessRequest processRequest) {
    return delegate.offer(processRequest);
  }

  @Override
  public ProcessRequest take() throws InterruptedException {
    return delegate.take();
  }

  @Override
  public ProcessRequest poll(long l, TimeUnit timeUnit) throws InterruptedException {
    return delegate.poll(l, timeUnit);
  }

  @Override
  public ProcessRequest poll() {
    return delegate.poll();
  }

  @Override
  public ProcessRequest peek() {
    return delegate.peek();
  }

  @Override
  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public boolean contains(Object o) {
    return delegate.contains(o);
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
  public String toString() {
    return delegate.toString();
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public int drainTo(
      Collection<? super ProcessRequest> collection) {
    return delegate.drainTo(collection);
  }

  @Override
  public int drainTo(
      Collection<? super ProcessRequest> collection, int i) {
    return delegate.drainTo(collection, i);
  }

  @Override
  public Iterator<ProcessRequest> iterator() {
    return delegate.iterator();
  }

  @Override
  public Spliterator<ProcessRequest> spliterator() {
    return delegate.spliterator();
  }

  @Override
  public boolean add(
      ProcessRequest processRequest) {
    return delegate.add(processRequest);
  }

  @Override
  public ProcessRequest remove() {
    return delegate.remove();
  }

  @Override
  public ProcessRequest element() {
    return delegate.element();
  }

  @Override
  public boolean addAll(
      Collection<? extends ProcessRequest> collection) {
    return delegate.addAll(collection);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return delegate.containsAll(collection);
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
  public boolean removeIf(
      Predicate<? super ProcessRequest> predicate) {
    return delegate.removeIf(predicate);
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public Stream<ProcessRequest> stream() {
    return delegate.stream();
  }

  @Override
  public Stream<ProcessRequest> parallelStream() {
    return delegate.parallelStream();
  }

  @Override
  public void forEach(
      Consumer<? super ProcessRequest> consumer) {
    delegate.forEach(consumer);
  }
}
