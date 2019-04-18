package io.github.cloudiator.deployment.scheduler.failure;

import com.google.inject.Singleton;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import io.github.cloudiator.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FailureHandler implements NodeFailureReportingInterface,
    ProcessFailureReportingInterface, ScheduleEventReportingInterface {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(FailureHandler.class);


  @Override
  public void addNodeFailure(Node node) {
    LOGGER.warn(String.format("Registering failure of node %s", node));

  }

  @Override
  public void addProcessFailure(CloudiatorProcess cloudiatorProcess) {
    LOGGER.warn(String.format("Registering failure of process %s", cloudiatorProcess));
  }

  @Override
  public void announceSchedule(Schedule schedule, ScheduleState from) {

  }

}
