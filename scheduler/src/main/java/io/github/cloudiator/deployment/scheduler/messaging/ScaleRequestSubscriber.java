package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobMessageRepository;
import io.github.cloudiator.deployment.scheduler.scaling.ScalingEngine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.ScheduleDomainRepository;
import java.util.ArrayList;
import java.util.List;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ScaleResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 23.05.2019.
 */
public class ScaleRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScaleRequestSubscriber.class);


  private final ProcessService processService;
  private final MessageInterface messageInterface;

  private final ScalingEngine scalingEngine;

  private final ScheduleDomainRepository scheduleDomainRepository;
  private final JobMessageRepository jobMessageRepository;

  private final NodeMessageRepository nodeMessageRepository;

  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public ScaleRequestSubscriber(
      ProcessService processService,
      MessageInterface messageInterface,
      ScheduleDomainRepository scheduleDomainRepository,
      JobMessageRepository jobMessageRepository,
      ScalingEngine scalingEngine,
      NodeMessageRepository nodeMessageRepository) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.scheduleDomainRepository = scheduleDomainRepository;
    this.jobMessageRepository = jobMessageRepository;
    this.scalingEngine = scalingEngine;

    this.nodeMessageRepository = nodeMessageRepository;
  }

  @Override
  public void run() {

    processService.subscribeScaleRequest(
        (id, content) -> {

          try {

            final String userId = content.getUserId();
            final String scheduleId = content.getScheduleId();
            final String taskId = content.getTaskId();
            Schedule schedule = scheduleDomainRepository
                .findByIdAndUser(scheduleId, content.getUserId());

            if (schedule == null) {
              messageInterface.reply(ScaleResponse.class, id, Error.newBuilder().setCode(404)
                  .setMessage(String.format("Schedule with id %s does not exist.", scheduleId))
                  .build());
              return;
            }

            Job job = jobMessageRepository
                .getById(userId, schedule.job());

            if (job == null) {
              messageInterface.reply(ScaleResponse.class, id, Error.newBuilder().setCode(404)
                  .setMessage(String.format("Job with id %s does not exist.", job))
                  .build());
              return;
            }

            if (!job.getTask(taskId).isPresent()) {
              messageInterface.reply(ScaleResponse.class, id, Error.newBuilder().setCode(404)
                  .setMessage(String.format("Job with id %s does not have task %s.", job, taskId))
                  .build());
              return;
            }

            Task task = job
                .getTask(taskId).get();

            List<Node> nodes = new ArrayList<>(content.getNodeCluster().getNodesCount());
            for (String nodeId : content.getNodeCluster().getNodesList()) {
              final Node byId = nodeMessageRepository.getById(userId, nodeId);
              if (byId == null) {
                messageInterface.reply(ScaleResponse.class, id, Error.newBuilder().setCode(404)
                    .setMessage(String.format("Node with id %s does not exist.", nodeId))
                    .build());
                return;
              }
              nodes.add(byId);
            }

            scalingEngine.scale(schedule, job, task, nodes);

            final ScaleResponse scaleResponse = ScaleResponse.newBuilder().build();

            messageInterface.reply(id, scaleResponse);

          } catch (Exception e) {
            final String errorMessage = String
                .format("Exception %s while processing request %s with id %s.", e.getMessage(),
                    content, id);

            LOGGER.error(errorMessage, e);

            messageInterface.reply(ScaleResponse.class, id,
                Error.newBuilder().setMessage(errorMessage).setCode(500).build());

          }


        });
  }

}
