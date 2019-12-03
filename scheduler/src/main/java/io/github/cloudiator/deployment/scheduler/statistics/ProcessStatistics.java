package io.github.cloudiator.deployment.scheduler.statistics;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorSingleProcess;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.messaging.NodeMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatistics {

  private final StatisticInterface statisticsInterface;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStatistics.class);
  private final NodeMessageRepository nodeMessageRepository;

  @Inject
  public ProcessStatistics(
      StatisticInterface statisticsInterface,
      NodeMessageRepository nodeMessageRepository) {
    this.statisticsInterface = statisticsInterface;
    this.nodeMessageRepository = nodeMessageRepository;
  }

  public void processStartupTime(CloudiatorProcess cloudiatorProcess, long time) {

    try {
      final MetricBuilder metricBuilder = MetricBuilder.create().name("process-start-time")
          .value(time)
          .now().addTag("id", cloudiatorProcess.id())
          .addTag("schedule", cloudiatorProcess.scheduleId())
          .addTag("type", cloudiatorProcess.type().name())
          .addTag("user", cloudiatorProcess.userId()).addTag("task", cloudiatorProcess.taskId())
          .addTag("taskInterface", cloudiatorProcess.taskInterface());

      if (cloudiatorProcess.originId().isPresent()) {
        metricBuilder.addTag("originId", cloudiatorProcess.originId().get());
      }

      if (cloudiatorProcess.endpoint().isPresent()) {
        metricBuilder.addTag("endpoint", cloudiatorProcess.endpoint().get());
      }

      if (cloudiatorProcess instanceof CloudiatorSingleProcess) {
        final Node node = nodeMessageRepository.getById(cloudiatorProcess.userId(),
            ((CloudiatorSingleProcess) cloudiatorProcess).node());

        if (node != null) {

          metricBuilder.addTag("node", node.id());
          if (node.originId().isPresent()) {
            metricBuilder.addTag("nodeOriginId", node.originId().get());
          }
          NodeProperties nodeProperties = node.nodeProperties();

          if (nodeProperties.numberOfCores().isPresent()) {
            metricBuilder.addTag("cores", nodeProperties.numberOfCores().get().toString());
          }
          if (nodeProperties.disk().isPresent()) {
            metricBuilder.addTag("disk", nodeProperties.disk().get().toString());
          }
          if (nodeProperties.memory().isPresent()) {
            metricBuilder.addTag("memory", nodeProperties.memory().get().toString());
          }

          if (nodeProperties.geoLocation().isPresent()) {
            GeoLocation geoLocation = nodeProperties.geoLocation().get();
            if (geoLocation.country().isPresent()) {
              metricBuilder.addTag("country", geoLocation.country().get());
            }
            if (geoLocation.city().isPresent()) {
              metricBuilder.addTag("city", geoLocation.city().get());
            }
          }

          if (nodeProperties.operatingSystem().isPresent()) {
            final OperatingSystem operatingSystem = nodeProperties.operatingSystem().get();
            metricBuilder
                .addTag("os_family", operatingSystem.operatingSystemFamily().name());
            metricBuilder.addTag("os_version",
                String.valueOf(operatingSystem.operatingSystemVersion().version()));
            metricBuilder
                .addTag("os_arch", operatingSystem.operatingSystemArchitecture().name());
          }

          node.nodeProperties();


        } else {
          LOGGER.warn(String.format("Could not find node %s. Skipping node information.",
              ((CloudiatorSingleProcess) cloudiatorProcess).node()));
        }

      }

      statisticsInterface.reportMetric(metricBuilder.build());

    } catch (Exception e) {
      LOGGER.warn("Error while reporting statistics for process " + cloudiatorProcess);
    }

  }
}
