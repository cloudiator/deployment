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

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.scheduler.ProcessStateMachine;
import io.github.cloudiator.deployment.scheduler.processes.ProcessStatusChecker.ProcessStatus;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.cloudiator.messaging.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWatchdog implements Schedulable {

  private final ProcessDomainRepository processDomainRepository;
  private final ProcessStatusChecker processStatusChecker;
  private final ProcessStateMachine processStateMachine;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProcessWatchdog.class);

  @Inject
  public ProcessWatchdog(
      ProcessDomainRepository processDomainRepository,
      ProcessStatusChecker processStatusChecker,
      ProcessStateMachine processStateMachine) {
    this.processDomainRepository = processDomainRepository;
    this.processStatusChecker = processStatusChecker;
    this.processStateMachine = processStateMachine;
  }

  @Override
  public long period() {
    return 1;
  }

  @Override
  public long delay() {
    return 1;
  }

  @Override
  public TimeUnit timeUnit() {
    return TimeUnit.MINUTES;
  }

  @Override
  public void run() {

    final List<CloudiatorProcess> processes = processDomainRepository.getAll();
    LOGGER.info(
        String.format("%s is starting a new run, watching %s process(es)", this, processes.size()));

    try {

      for (CloudiatorProcess cloudiatorProcess : processes) {

        //only check running processes
        if (cloudiatorProcess.state().equals(ProcessState.RUNNING)) {

          LOGGER
              .debug(String.format("%s is checking state of process %s.", this, cloudiatorProcess));

          try {
            final ProcessStatus processStatus = processStatusChecker.checkState(cloudiatorProcess);

            if (!processStatus.processState().equals(cloudiatorProcess.state())) {

              LOGGER.warn(String.format(
                  "Remote process state %s is different from local process state %s. Updating the state.",
                  processStatus.processState(), cloudiatorProcess.state()));

              if (processStatus.processState().equals(ProcessState.ERROR)) {
                processStateMachine.fail(cloudiatorProcess, null,
                    new IllegalStateException(processStatus.information().orElse(null)));
              } else {
                processStateMachine.apply(cloudiatorProcess, processStatus.processState(), null);
              }

            }


          } catch (ResponseException e) {
            LOGGER.warn(String
                .format("Error %s while checking state of process %s. Ignoring.", e.getMessage(),
                    cloudiatorProcess), e);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn(String
          .format("%s encountered unexpected error %s. Caught to allow further execution.", this,
              e.getMessage()), e);
    }

    LOGGER.info(String.format("%s is finished its run", this));

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .toString();
  }
}
