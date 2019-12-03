package io.github.cloudiator.deployment.scheduler.messaging;


import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.deployment.scheduler.failure.ProcessFailureReportingInterface;
import io.github.cloudiator.deployment.scheduler.processes.ProcessCleanup;
import org.cloudiator.messages.Process.ProcessEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.services.ProcessService;

public class ProcessEventSubscriber implements Runnable {

  private final ProcessService processService;
  private final ProcessFailureReportingInterface processFailureReportingInterface;
  private final ProcessCleanup processCleanup;

  @Inject
  public ProcessEventSubscriber(ProcessService processService,
      ProcessFailureReportingInterface processFailureReportingInterface,
      ProcessCleanup processCleanup) {
    this.processService = processService;
    this.processFailureReportingInterface = processFailureReportingInterface;
    this.processCleanup = processCleanup;
  }

  @Override
  public void run() {

    processService.subscribeProcessEvent(new MessageCallback<ProcessEvent>() {
      @Override
      public void accept(String id, ProcessEvent content) {

        CloudiatorProcess cloudiatorProcess = ProcessMessageConverter.INSTANCE
            .apply(content.getProcess());

        if (cloudiatorProcess.state().equals(ProcessState.ERROR)) {
          processFailureReportingInterface.addProcessFailure(cloudiatorProcess);
        }

        if (cloudiatorProcess.state().equals(ProcessState.FINISHED)) {
          processCleanup.finishProcess(cloudiatorProcess);
        }

      }
    });

  }
}
