package io.github.cloudiator.deployment.scheduler.messaging;


import com.google.inject.Inject;
import org.cloudiator.messages.Process.ProcessEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.services.ProcessService;

public class ProcessEventSubscriber implements Runnable {

  private final ProcessService processService;

  @Inject
  public ProcessEventSubscriber(ProcessService processService) {
    this.processService = processService;
  }

  @Override
  public void run() {

    processService.subscribeProcessEvent(new MessageCallback<ProcessEvent>() {
      @Override
      public void accept(String id, ProcessEvent content) {
        
      }
    });

  }
}
