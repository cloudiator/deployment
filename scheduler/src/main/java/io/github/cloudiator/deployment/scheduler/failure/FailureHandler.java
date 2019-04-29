package io.github.cloudiator.deployment.scheduler.failure;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.deployment.scheduler.ScheduleStateMachine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FailureHandler implements NodeFailureReportingInterface,
    ProcessFailureReportingInterface, ScheduleEventReportingInterface {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(FailureHandler.class);

  private final ScheduleDomainRepository scheduleDomainRepository;
  private final ScheduleStateMachine scheduleStateMachine;

  @Inject
  public FailureHandler(
      ScheduleDomainRepository scheduleDomainRepository,
      ScheduleStateMachine scheduleStateMachine) {
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.scheduleStateMachine = scheduleStateMachine;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Schedule findSchedule(String scheduleId, String userId) {
    return scheduleDomainRepository
        .findByIdAndUser(scheduleId, userId);
  }

  @Override
  public void addNodeFailure(Node node) {
    //todo: currently ignoring node failures if the process is not also failing
    LOGGER.warn(
        String.format("Registering failure of node %s. Ignoring until process also fails..", node));
  }

  @Override
  public synchronized void addProcessFailure(CloudiatorProcess cloudiatorProcess) {
    LOGGER.warn(String.format("Registering failure of process %s", cloudiatorProcess));

    final Schedule schedule = findSchedule(cloudiatorProcess.scheduleId(),
        cloudiatorProcess.userId());

    checkNotNull(schedule, String
        .format("Process %s failed, but schedule with id %s does not exist.", cloudiatorProcess,
            cloudiatorProcess.scheduleId()));

    //if the schedule is not in state running ignore it
    if (schedule.state().equals(ScheduleState.RUNNING)) {
      //wait for multiple processes to fail?
      scheduleStateMachine.fail(schedule, null,
          new IllegalStateException(String.format("Process %s failed.", cloudiatorProcess)));
    } else {
      LOGGER.warn(String
          .format("Process %s failed. Ignoring it as schedule is in state %s", cloudiatorProcess,
              schedule.state()));
    }
  }

  private void handleAffectedProcesses(CloudiatorProcess cloudiatorProcess) {



  }

  @Override
  public void announceSchedule(Schedule schedule, ScheduleState from) {

    switch (schedule.state()) {
      case RUNNING:
        if (!checkSchedule(schedule)) {
          LOGGER.warn(String
              .format("Failing schedule %s as one or multiple processes have failed.", schedule));
          scheduleStateMachine.fail(schedule, null,
              new IllegalStateException("One or multiple processes have failed"));
        }
        break;
      case ERROR:
        LOGGER.warn(String.format("Schedule %s has failed. Triggering restore.", schedule));
        triggerRestore(schedule);
        break;
      case RESTORING:
        scheduleStateMachine.apply(schedule, ScheduleState.RUNNING, null);
        break;
      default:
        LOGGER.debug(
            String.format("Receiving schedule event for schedule %s but ignoring it.", schedule));
    }

  }

  private boolean checkSchedule(Schedule schedule) {

    for (CloudiatorProcess cloudiatorProcess : schedule.processes()) {
      if (cloudiatorProcess.state().equals(ProcessState.ERROR)) {
        return false;
      }
    }

    return true;
  }

  private void triggerRestore(Schedule schedule) {
    scheduleStateMachine.apply(schedule, ScheduleState.RESTORING, null);
  }

}
