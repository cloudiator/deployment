package io.github.cloudiator.deployment.scheduler;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import org.cloudiator.messages.Process.CreateFaasProcessRequest;
import org.cloudiator.messages.entities.ProcessEntities.Process;
import org.cloudiator.messages.entities.ProcessEntities.FaasProcess;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;

public class FaasProcessSpawner implements ProcessSpawner {

  private final ProcessService processService;
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();
  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;

  @Inject
  public FaasProcessSpawner(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public boolean supports(Task task) {
    return task.interfaces().stream()
        .allMatch(iface -> iface instanceof FaasInterface);
  }

  @Override
  public CloudiatorProcess spawn(String userId, String schedule, Job job, Task task, Node node) {
    final FaasProcess faasProcess = FaasProcess.newBuilder()
        .setSchedule(schedule)
        .setJob(JOB_CONVERTER.applyBack(job))
        .setNode(NODE_MESSAGE_CONVERTER.apply(node)).setTask(task.name()).build();
    CreateFaasProcessRequest processRequest = CreateFaasProcessRequest.newBuilder()
        .setFaas(faasProcess)
        .setUserId(userId).build();

    try {
      Process process = processService.createFaasProcess(processRequest).getProcess();
      return PROCESS_MESSAGE_CONVERTER.apply(process);
    } catch (ResponseException e) {
      throw new IllegalStateException("Error while creating process.", e.getCause());
    }
  }
}
