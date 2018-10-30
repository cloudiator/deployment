package io.github.cloudiator.deployment.faasagent.messaging;

import com.amazonaws.regions.Regions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import io.github.cloudiator.deployment.domain.*;
import io.github.cloudiator.deployment.faasagent.cloudformation.Deployer;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.persistance.FunctionDomainRepository;
import org.cloudiator.messages.General;
import org.cloudiator.messages.Process.CreateFaasProcessRequest;
import org.cloudiator.messages.Process.FaasProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.Process;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;

public class CreateFaasProcessSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateFaasProcessSubscriber.class);
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;

  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final CloudMessageRepository cloudMessageRepository;
  private final FunctionDomainRepository functionDomainRepository;

  @Inject
  public CreateFaasProcessSubscriber(
      ProcessService processService,
      MessageInterface messageInterface,
      CloudMessageRepository cloudMessageRepository,
      FunctionDomainRepository functionDomainRepository) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.cloudMessageRepository = cloudMessageRepository;
    this.functionDomainRepository = functionDomainRepository;
  }

  @Override
  public void run() {
    processService.subscribeCreateFaasProcessRequest(
        (id, content) -> {
          try {
            LOGGER.debug("Faas agent received process request {}", content);
            final String userId = content.getUserId();
            final String nodeId = content.getFaas().getNode().getId();
            final Function function = functionDomainRepository.findByIdAndTenant(nodeId, userId);
            final Cloud cloud = cloudMessageRepository.getById(userId, function.cloudId());

            ApplicationTemplate appTemplate = convertToTemplate(content);
            CloudCredential credential = cloud.credential();
            // TODO get provider from node and select appropriate deployer
            // Use composite pattern
            Deployer deployer = new Deployer(appTemplate, credential.user(), credential.password());
            String apiId = deployer.deployApp();

            final FaasProcessCreatedResponse faasProcessCreatedResponse =
                FaasProcessCreatedResponse.newBuilder()
                    .setProcess(Process.newBuilder()
                        .setId(apiId)
                        .setSchedule(content.getFaas().getSchedule())
                        .setNode(content.getFaas().getNode().getId())
                        .setTask(content.getFaas().getTask()))
                    .build();
            messageInterface.reply(id, faasProcessCreatedResponse);
          } catch (Exception e) {
            final String errorMessage = MessageFormat.format(
                "Exception {0} while processing request {1}.with id {2}.",
                e.getMessage(), // 0
                content, // 1
                id); // 2

            LOGGER.error(errorMessage, e);
            messageInterface.reply(FaasProcessCreatedResponse.class, id,
                General.Error.newBuilder().setMessage(errorMessage).setCode(500).build());
          }
        }
    );
  }

  private ApplicationTemplate convertToTemplate(CreateFaasProcessRequest request) {
    final Job job = JOB_CONVERTER.apply(request.getFaas().getJob());
    final String taskName = request.getFaas().getTask();
    final Task task = job.getTask(taskName)
        .orElseThrow(() -> new IllegalStateException(
            MessageFormat.format("Job {0} does not contain task {1}", job, taskName)));

    ApplicationTemplate applicationTemplate = new ApplicationTemplate();
    applicationTemplate.name = task.name();
    applicationTemplate.region = Regions.EU_WEST_1; // TODO get from functionModel
    applicationTemplate.functions = new ArrayList<>();
    for (TaskInterface taskInterface : task.interfaces()) {
      if (taskInterface instanceof FaasInterface) {
        FaasInterface faasInterface = (FaasInterface) taskInterface;
        LambdaTemplate lambda = new LambdaTemplate();
        lambda.name = faasInterface.functionName();
        lambda.codeUrl = faasInterface.sourceCodeUrl();
        for (Trigger trigger : faasInterface.triggers()) {
          if (trigger instanceof HttpTrigger) {
            HttpTrigger httpTrigger = (HttpTrigger) trigger;
            lambda.httpPath = httpTrigger.httpPath();
            lambda.httpMethod = httpTrigger.httpMethod();
          }
        }
        lambda.handler = faasInterface.handler();
        lambda.memorySize = faasInterface.memory();
        lambda.timeout = faasInterface.timeout();
        lambda.runtime = convertRuntime(faasInterface.runtime());
        applicationTemplate.functions.add(lambda);
      }
    }
    return applicationTemplate;
  }

  private String convertRuntime(String runtime) {
    return ImmutableMap.of(
        "nodejs", "nodejs8.10",
        "python", "python3.6",
        "java", "java8",
        "dotnet", "dotnetcore2.1",
        "go", "go1.x"
    ).computeIfAbsent(runtime, rt -> {
      throw new IllegalStateException("unknown runtime " + rt);
    });
  }

}
