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

package io.github.cloudiator.deployment.scheduler.processes;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import java.util.Optional;
import javax.annotation.Nullable;
import org.cloudiator.messaging.ResponseException;

public interface ProcessStatusChecker {

  class ProcessStatus {

    private final ProcessState processState;
    @Nullable
    private final String information;

    public ProcessStatus(
        ProcessState processState, @Nullable String information) {
      this.processState = processState;
      this.information = information;
    }

    public static ProcessStatus of(ProcessState processState, @Nullable String information) {
      checkNotNull(processState, "processState is null");
      return new ProcessStatus(processState, information);
    }

    public ProcessState processState() {
      return processState;
    }

    public Optional<String> information() {
      return Optional.ofNullable(information);
    }
  }

  ProcessStatus checkState(CloudiatorProcess cloudiatorProcess) throws ResponseException;

}
