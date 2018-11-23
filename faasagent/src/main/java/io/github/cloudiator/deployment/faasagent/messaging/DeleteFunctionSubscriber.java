package io.github.cloudiator.deployment.faasagent.messaging;

import com.amazonaws.regions.Regions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.deployment.faasagent.cloudformation.AwsDeployer;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer.FaasDeployerFactory;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.messaging.LocationMessageRepository;
import io.github.cloudiator.persistance.FunctionDomainRepository;
import io.github.cloudiator.deployment.domain.Function;
import org.cloudiator.messages.Function.DeleteFunctionRequestMessage;
import org.cloudiator.messages.General;
import org.cloudiator.messages.Function.FunctionDeletedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

public class DeleteFunctionSubscriber implements Runnable {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteFunctionSubscriber.class);
  private final MessageInterface messageInterface;
  private final FunctionDomainRepository functionDomainRepository;
  private final CloudMessageRepository cloudMessageRepository;
  private final LocationMessageRepository locationMessageRepository;
  private final FaasDeployerFactory faasDeployerFactory;

  @Inject
  public DeleteFunctionSubscriber(
      MessageInterface messageInterface,
      FunctionDomainRepository functionDomainRepository,
      CloudMessageRepository cloudMessageRepository,
      LocationMessageRepository locationMessageRepository,
      FaasDeployerFactory faasDeployerFactory) {
    this.messageInterface = messageInterface;
    this.functionDomainRepository = functionDomainRepository;
    this.cloudMessageRepository = cloudMessageRepository;
    this.locationMessageRepository = locationMessageRepository;
    this.faasDeployerFactory = faasDeployerFactory;
  }

  @Override
  public void run() {
    messageInterface.subscribe(DeleteFunctionRequestMessage.class,
        DeleteFunctionRequestMessage.parser(),
        (requestId, request) -> {
          try {
            final String userId = request.getUserId();
            final String functionId = request.getFunctionId();

            LOGGER.debug("{} retrieved request to delete function with id {} for user {}.",
                this, functionId, userId);

            Function function = functionDomainRepository.findByIdAndTenant(functionId, userId);
            checkState(function != null, String.format("Function with id %s does not exist.", functionId));
            final Cloud cloud = cloudMessageRepository.getById(userId, function.cloudId());
            String region = locationMessageRepository.
                getRegionName(userId, function.locationId());
            faasDeployerFactory.create(region, cloud).removeApp(function.stackId());
            deleteFunction(functionId, userId);
            messageInterface.reply(requestId, FunctionDeletedResponse.newBuilder().build());
          } catch (Exception e) {
            LOGGER.error("Unexpected exception while deleting function: " + e.getMessage(),
                e);
            messageInterface.reply(FunctionDeletedResponse.class, requestId,
                General.Error.newBuilder().setCode(500).setMessage(
                    "Unexpected exception while deleting function: " + e.getMessage())
                    .build());
          }

        });
  }

  @Transactional
  void deleteFunction(String functionId, String userId) {
    functionDomainRepository.delete(functionId, userId);
  }
}
