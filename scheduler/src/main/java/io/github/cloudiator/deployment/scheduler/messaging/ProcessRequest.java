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

import static com.google.common.base.Preconditions.checkNotNull;

import org.cloudiator.messages.Process.CreateProcessRequest;

public class ProcessRequest {

  private final String id;
  private final CreateProcessRequest createProcessRequest;

  public static ProcessRequest of(String id, CreateProcessRequest createProcessRequest) {
    checkNotNull(id, "id is null");
    checkNotNull(createProcessRequest, "createProcessRequest is null");
    return new ProcessRequest(id, createProcessRequest);
  }

  private ProcessRequest(String id,
      CreateProcessRequest createProcessRequest) {
    this.id = id;
    this.createProcessRequest = createProcessRequest;
  }

  public String getId() {
    return id;
  }

  public CreateProcessRequest getCreateProcessRequest() {
    return createProcessRequest;
  }
}
