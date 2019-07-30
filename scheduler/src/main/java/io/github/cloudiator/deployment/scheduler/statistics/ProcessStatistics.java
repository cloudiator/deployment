package io.github.cloudiator.deployment.scheduler.statistics;

import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatistics {

  private final StatisticInterface statisticsInterface;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStatistics.class);

  public ProcessStatistics(
      StatisticInterface statisticsInterface) {
    this.statisticsInterface = statisticsInterface;
  }

  public void processStartupTime(String user, CloudiatorProcess cloudiatorProcess, long time) {

    try {
      final MetricBuilder metricBuilder = MetricBuilder.create().name("process-start-time")
          .value(time)
          .now().addTag("id", cloudiatorProcess.id())
          .addTag("type", cloudiatorProcess.type().name())
          .addTag("user", user).addTag("task", cloudiatorProcess.taskId());

      statisticsInterface.reportMetric(metricBuilder.build());

    } catch (Exception e) {
      LOGGER.warn("Error while reporting statistics for process " + cloudiatorProcess);
    }

  }
}
