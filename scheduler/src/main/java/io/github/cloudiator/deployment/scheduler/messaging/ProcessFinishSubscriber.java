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

package io.github.cloudiator.deployment.scheduler.messaging;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.ProcessStateMachine;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.FinishProcessRequest;
import org.cloudiator.messages.Process.FinishProcessResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;

public class ProcessFinishSubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final ProcessDomainRepository processDomainRepository;
  private final JobMessageRepository jobMessageRepository;
  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ProcessStateMachine processStateMachine;

  @Inject
  public ProcessFinishSubscriber(MessageInterface messageInterface,
      ProcessDomainRepository processDomainRepository,
      JobMessageRepository jobMessageRepository,
      ScheduleDomainRepository scheduleDomainRepository,
      ProcessStateMachine processStateMachine) {
    this.messageInterface = messageInterface;
    this.processDomainRepository = processDomainRepository;
    this.jobMessageRepository = jobMessageRepository;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.processStateMachine = processStateMachine;
  }


  @SuppressWarnings("WeakerAccess")
  @Transactional
  protected void handle(String requestId, FinishProcessRequest request) {

    final CloudiatorProcess cloudiatorProcess = processDomainRepository
        .getById(request.getProcessId());

    if (cloudiatorProcess == null) {
      messageInterface.reply(FinishProcessResponse.class, requestId,
          Error.newBuilder().setCode(404).setMessage(String
              .format("Process with the id %s does not exist.", request.getProcessId()))
              .build());
      return;
    }

    final Schedule schedule = scheduleDomainRepository.findByProcess(cloudiatorProcess);
    checkState(schedule != null,
        String.format("Schedule with id %s does not exist", cloudiatorProcess.scheduleId()));

    final Job job = jobMessageRepository.getById(cloudiatorProcess.userId(), schedule.job());
    checkState(job != null, String.format("Job with id %s does not exist.", schedule.job()));

    final Task task = job.getTask(cloudiatorProcess.taskId())
        .orElseThrow(() -> new IllegalStateException("Could not find task of process"));

    if (!task.canFinish(task.interfaceOfName(cloudiatorProcess.taskInterface()))) {
      messageInterface.reply(FinishProcessResponse.class, requestId,
          Error.newBuilder().setCode(400).setMessage(String
              .format("Process with the id %s can not finish.", request.getProcessId()))
              .build());
      return;
    }

    if (!cloudiatorProcess.state().equals(ProcessState.RUNNING)) {
      messageInterface.reply(FinishProcessResponse.class, requestId,
          Error.newBuilder().setCode(400).setMessage(String
              .format("Only running processes can finish. Process however is in state %s.",
                  cloudiatorProcess.state()))
              .build());
      return;
    }

    //check if secret is correct
    if (!cloudiatorProcess.secret().isPresent() || !cloudiatorProcess.secret().get()
        .equals(request.getSecret())) {
      messageInterface.reply(FinishProcessResponse.class, requestId,
          Error.newBuilder().setCode(403).setMessage("Supplied secret is incorrect")
              .build());
      return;
    }

    processStateMachine.apply(cloudiatorProcess, ProcessState.FINISHED, null);

    messageInterface.reply(requestId, FinishProcessResponse.newBuilder().build());
  }


  @Override
  public void run() {
    messageInterface.subscribe(FinishProcessRequest.class, FinishProcessRequest.parser(),
        new MessageCallback<FinishProcessRequest>() {
          @Override
          public void accept(String id, FinishProcessRequest content) {
            handle(id, content);
          }
        });
  }
}
