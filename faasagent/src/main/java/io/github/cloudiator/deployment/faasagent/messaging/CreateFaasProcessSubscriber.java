package io.github.cloudiator.deployment.faasagent.messaging;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.deployment.domain.FaasInterface;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.deployment.domain.FunctionBuilder;
import io.github.cloudiator.deployment.domain.HttpTrigger;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.Trigger;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer.FaasDeployerFactory;
import io.github.cloudiator.deployment.messaging.FaasInterfaceConverter;
import io.github.cloudiator.deployment.messaging.JobConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.Runtime;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.messaging.LocationMessageRepository;
import io.github.cloudiator.messaging.NodeMessageRepository;
import io.github.cloudiator.persistance.FunctionDomainRepository;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.cloudiator.messages.General;
import org.cloudiator.messages.Process.CreateFaasProcessRequest;
import org.cloudiator.messages.Process.FaasProcessCreatedResponse;
import org.cloudiator.messages.entities.ProcessEntities.Process;
import org.cloudiator.messages.entities.ProcessEntities.ProcessState;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateFaasProcessSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateFaasProcessSubscriber.class);
  private static final JobConverter JOB_CONVERTER = JobConverter.INSTANCE;
  private static final int RANDOM_LEN = 8;

  private final ProcessService processService;
  private final MessageInterface messageInterface;
  private final CloudMessageRepository cloudMessageRepository;
  private final NodeMessageRepository nodeMessageRepository;
  private final FunctionDomainRepository functionDomainRepository;
  private final LocationMessageRepository locationMessageRepository;
  private final FaasDeployerFactory faasDeployerFactory;


  @Inject
  public CreateFaasProcessSubscriber(
      ProcessService processService,
      MessageInterface messageInterface,
      CloudMessageRepository cloudMessageRepository,
      LocationMessageRepository locationMessageRepository,
      NodeMessageRepository nodeMessageRepository,
      FunctionDomainRepository functionDomainRepository,
      FaasDeployerFactory faasDeployerFactory) {
    this.processService = processService;
    this.messageInterface = messageInterface;
    this.cloudMessageRepository = cloudMessageRepository;
    this.locationMessageRepository = locationMessageRepository;
    this.nodeMessageRepository = nodeMessageRepository;
    this.functionDomainRepository = functionDomainRepository;
    this.faasDeployerFactory = faasDeployerFactory;
  }

  @Override
  public void run() {
    processService.subscribeCreateFaasProcessRequest(
        (id, content) -> {
          try {
            LOGGER.debug("Faas agent received process request {}", content);
            final String userId = content.getUserId();
            final String nodeId = content.getFaas().getNode().getId();
            final Node node = nodeMessageRepository.getById(userId, nodeId);
            if (node == null) {
              LOGGER.error("Node with ID {} doesn't exits", nodeId);
              throw new IllegalStateException("Node doesn't exists: " + nodeId);
            }
            if (node.originId() == null || !node.originId().isPresent()) {
              throw new IllegalStateException("Node origin ID is null");
            }
            final Function function = functionDomainRepository
                .findByIdAndTenant(node.originId().get(), userId);
            final Cloud cloud = cloudMessageRepository.getById(userId, function.cloudId());

            ApplicationTemplate appTemplate = convertToTemplate(content, function);
            String apiId = faasDeployerFactory
                .create(appTemplate.region, cloud)
                .deployApp(appTemplate);

            final Function newFunction = FunctionBuilder.newBuilder(function)
                .stackId(appTemplate.name).build();

            persistFunction(newFunction, userId);

            final FaasProcessCreatedResponse faasProcessCreatedResponse =
                FaasProcessCreatedResponse.newBuilder()
                    .setProcess(Process.newBuilder()
                        .setId(apiId)
                        .setOriginId(apiId)
                        .setTaskInterface(FaasInterface.class.getCanonicalName())
                        .setUserId(userId)
                        .setState(ProcessState.PROCESS_STATE_RUNNING)
                        .setSchedule(content.getFaas().getSchedule())
                        //TODO: add refactor to nodegroup here
                        .setNode(content.getFaas().getNode().getId())
                        .setTask(content.getFaas().getTask()))
                    .build();
            messageInterface.reply(id, faasProcessCreatedResponse);
          } catch (Exception e) {

            final String errorMessage = MessageFormat.format(
                "Exception {0} while processing request {1} with id {2}.",
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

  @Transactional
  void persistFunction(Function function, String userId) {
    functionDomainRepository.save(function, userId);
  }

  private ApplicationTemplate convertToTemplate(CreateFaasProcessRequest request,
      Function function) {
    final Job job = JOB_CONVERTER.apply(request.getFaas().getJob());
    final String taskName = request.getFaas().getTask();
    final Task task = job.getTask(taskName)
        .orElseThrow(() -> new IllegalStateException(
            MessageFormat.format("Job {0} does not contain task {1}", job, taskName)));

    ApplicationTemplate applicationTemplate = new ApplicationTemplate();
    // Generate app name in format TaskName-RandomString
    applicationTemplate.name = SdkContext.randomResourceName(
        task.name() + '-', taskName.length() + 1 + RANDOM_LEN);
    applicationTemplate.region = locationMessageRepository
        .getRegionName(request.getUserId(), function.locationId());
    applicationTemplate.functions = new ArrayList<>();

    FaasInterface faasInterface = FaasInterfaceConverter.INSTANCE
        .apply(request.getFaas().getFaasInterface());

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
    lambda.memorySize = function.memory();
    lambda.timeout = faasInterface.timeout();
    lambda.runtime = convertRuntime(function.runtime());
    lambda.env = faasInterface.functionEnvironment();
    applicationTemplate.functions.add(lambda);

    return applicationTemplate;
  }

  // Get AWS compatible runtime string
  private String convertRuntime(Runtime runtime) {
    return ImmutableMap.of(
        Runtime.NODEJS, "nodejs8.10",
        Runtime.PYTHON, "python2.7",
        Runtime.JAVA, "java8",
        Runtime.DOTNET, "dotnetcore2.1",
        Runtime.GO, "go1.x"
    ).computeIfAbsent(runtime, rt -> {
      throw new IllegalStateException("unknown runtime " + rt);
    });
  }

}
