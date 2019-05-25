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

package io.github.cloudiator.deployment.scheduler.instantiation;

import com.google.common.util.concurrent.ListenableFuture;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import java.util.List;
import javax.annotation.Nullable;


public interface ResourcePool {

  List<ListenableFuture<Node>> allocate(Schedule schedule,
      Iterable<? extends NodeCandidate> nodeCandidates, @Nullable String name);

  List<ListenableFuture<Node>> allocate(Schedule schedule,
      Iterable<? extends NodeCandidate> nodeCandidates, Iterable<? extends Node> existingResources,
      @Nullable String name);
}
