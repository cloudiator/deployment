package io.github.cloudiator.deployment.scheduler.statistics;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.deployment.domain.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleStatistics {

  private final StatisticInterface statisticsInterface;
  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleStatistics.class);

  @Inject
  public ScheduleStatistics(
      StatisticInterface statisticsInterface) {
    this.statisticsInterface = statisticsInterface;
  }

  public void initialStartTime(Schedule schedule, long time) {

    try {
      final MetricBuilder metricBuilder = MetricBuilder.create().name("schedule-start-time")
          .value(time)
          .now().addTag("id", schedule.id())
          .addTag("instantiation", schedule.instantiation().name())
          .addTag("number_processes", String.valueOf(schedule.processes().size()))
          .addTag("number_nodes", String.valueOf(schedule.nodes().size()))
          .addTag("user", schedule.userId()).addTag("job", schedule.job());

      statisticsInterface.reportMetric(metricBuilder.build());

    } catch (Exception e) {
      LOGGER.warn("Error while reporting statistics for schedule: " + schedule);
    }

  }
}
