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

package io.github.cloudiator.deployment.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcess;
import io.github.cloudiator.deployment.domain.CloudiatorClusterProcessBuilder;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcessBuilder;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.processes.ProcessKiller;
import io.github.cloudiator.deployment.scheduler.processes.ProcessScheduler;
import io.github.cloudiator.deployment.scheduler.processes.ProcessSpawningException;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.cloudiator.messages.Process.ProcessEvent;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProcessStateMachine implements ErrorAwareStateMachine<CloudiatorProcess> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStateMachine.class);
  private final ErrorAwareStateMachine<CloudiatorProcess> stateMachine;
  private final ProcessDomainRepository processDomainRepository;
  private final ProcessKiller processKiller;
  private final ProcessScheduler processScheduler;

  @Inject
  public ProcessStateMachine(ProcessService processService,
      ProcessDomainRepository processDomainRepository,
      ProcessKiller processKiller,
      ProcessScheduler processScheduler) {
    this.processDomainRepository = processDomainRepository;

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<CloudiatorProcess>builder().errorTransition(error())
        .addTransition(
            Transitions.<CloudiatorProcess>transitionBuilder().from(ProcessState.PENDING)
                .to(ProcessState.RUNNING)
                .action(pendingToRunning())
                .build())
        .addTransition(
            Transitions.<CloudiatorProcess>transitionBuilder().from(ProcessState.RUNNING)
                .to(ProcessState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<CloudiatorProcess>transitionBuilder().from(ProcessState.ERROR)
                .to(ProcessState.DELETED)
                .action(delete())
                .build())
        .addHook(new StateMachineHook<CloudiatorProcess>() {
          @Override
          public void pre(CloudiatorProcess process, State to) {
            //intentionally left empty
          }

          @Override
          public void post(State from, CloudiatorProcess process) {

            final ProcessEvent processEvent = ProcessEvent.newBuilder()
                .setProcess(ProcessMessageConverter.INSTANCE.applyBack(process))
                .setFrom(ProcessMessageConverter.PROCESS_STATE_CONVERTER.applyBack(
                    (CloudiatorProcess.ProcessState) from))
                .setTo(ProcessMessageConverter.PROCESS_STATE_CONVERTER.applyBack(process.state()))
                .build();

            LOGGER.debug(String
                .format(
                    "Executing post hook to announce process changed event for process %s. Previous state was %s, new state is %s.",
                    process, from, process.state()));
            processService.announceProcessEvent(processEvent);
          }
        })
        .build();
    this.processKiller = processKiller;
    this.processScheduler = processScheduler;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  CloudiatorProcess save(CloudiatorProcess process) {
    processDomainRepository.save(process);
    return process;
  }

  private static CloudiatorProcess updateProcess(CloudiatorProcess process, ProcessState newState,
      @Nullable String diagnostic) {
    if (process instanceof CloudiatorClusterProcess) {

      final CloudiatorClusterProcessBuilder cloudiatorClusterProcessBuilder = CloudiatorClusterProcessBuilder
          .of((CloudiatorClusterProcess) process).state(newState);
      if (diagnostic != null) {
        cloudiatorClusterProcessBuilder.diagnostic(diagnostic);
      }
      return cloudiatorClusterProcessBuilder.build();


    } else if (process instanceof CloudiatorSingleProcess) {
      final CloudiatorSingleProcessBuilder cloudiatorSingleProcessBuilder = CloudiatorSingleProcessBuilder
          .of((CloudiatorSingleProcess) process).state(newState);

      if (diagnostic != null) {
        cloudiatorSingleProcessBuilder.diagnostic(diagnostic);
      }

      return cloudiatorSingleProcessBuilder.build();


    } else {
      throw new AssertionError("Unknown process type " + process.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void delete(CloudiatorProcess process) {
    processDomainRepository.delete(process.id(), process.userId());
  }

  private TransitionAction<CloudiatorProcess> pendingToRunning() {

    return (o, arguments) -> {

      try {
        processScheduler.schedule(o);
        final CloudiatorProcess running = updateProcess(o, ProcessState.RUNNING, null);
        save(running);
        return running;
      } catch (ProcessSpawningException e) {
        throw new ExecutionException("Error while scheduling process.", e);
      }
    };
  }

  private TransitionAction<CloudiatorProcess> delete() {

    return (process, arguments) -> {

      processKiller.kill(process);
      delete(process);

      return updateProcess(process, ProcessState.DELETED, null);
    };
  }

  private ErrorTransition<CloudiatorProcess> error() {

    return Transitions.<CloudiatorProcess>errorTransitionBuilder()
        .action((o, arguments, throwable) -> {

          final String message = throwable != null ? throwable.getMessage() : null;

          return save(updateProcess(o, ProcessState.ERROR, message));
        }).errorState(ProcessState.ERROR).build();
  }


  @Override
  public CloudiatorProcess apply(CloudiatorProcess object, State to, Object[] arguments) {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public CloudiatorProcess fail(CloudiatorProcess object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
