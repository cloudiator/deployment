/*
 * Copyright 2014-2019 University of Ulm
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Environment implements Map<String,String> {

  private final Map<String,String> delegate;

  public Environment(Map<String, String> delegate) {
    this.delegate = delegate;
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
  public boolean containsKey(Object o) {
    return delegate.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return delegate.containsValue(o);
  }

  @Override
  public String get(Object o) {
    return delegate.get(o);
  }

  @Override
  public String put(String s, String s2) {
    return delegate.put(s, s2);
  }

  @Override
  public String remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> map) {
    delegate.putAll(map);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<String> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return delegate.entrySet();
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
  public String getOrDefault(Object o, String s) {
    return delegate.getOrDefault(o, s);
  }

  @Override
  public void forEach(
      BiConsumer<? super String, ? super String> biConsumer) {
    delegate.forEach(biConsumer);
  }

  @Override
  public void replaceAll(
      BiFunction<? super String, ? super String, ? extends String> biFunction) {
    delegate.replaceAll(biFunction);
  }

  @Override
  public String putIfAbsent(String s, String s2) {
    return delegate.putIfAbsent(s, s2);
  }

  @Override
  public boolean remove(Object o, Object o1) {
    return delegate.remove(o, o1);
  }

  @Override
  public boolean replace(String s, String s2, String v1) {
    return delegate.replace(s, s2, v1);
  }

  @Override
  public String replace(String s, String s2) {
    return delegate.replace(s, s2);
  }

  @Override
  public String computeIfAbsent(String s,
      Function<? super String, ? extends String> function) {
    return delegate.computeIfAbsent(s, function);
  }

  @Override
  public String computeIfPresent(String s,
      BiFunction<? super String, ? super String, ? extends String> biFunction) {
    return delegate.computeIfPresent(s, biFunction);
  }

  @Override
  public String compute(String s,
      BiFunction<? super String, ? super String, ? extends String> biFunction) {
    return delegate.compute(s, biFunction);
  }

  @Override
  public String merge(String s, String s2,
      BiFunction<? super String, ? super String, ? extends String> biFunction) {
    return delegate.merge(s, s2, biFunction);
  }
}
