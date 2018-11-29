package io.github.cloudiator.deployment.faasagent.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.messaging.FunctionConverter;
import io.github.cloudiator.persistance.FunctionDomainRepository;
import org.cloudiator.messages.Function;
import org.cloudiator.messages.Function.FunctionQueryMessage;
import org.cloudiator.messages.Function.FunctionQueryResponse;
import org.cloudiator.messages.General;
import org.cloudiator.messages.entities.FaasEntities;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final FunctionDomainRepository functionDomainRepository;
  private static final FunctionConverter FUNCTION_CONVERTER = new FunctionConverter();

  @Inject
  public FunctionQuerySubscriber(
      MessageInterface messageInterface,
      FunctionDomainRepository functionDomainRepository) {
    this.messageInterface = messageInterface;
    this.functionDomainRepository = functionDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(FunctionQueryMessage.class, FunctionQueryMessage.parser(),
        (id, request) -> {
          try {
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
              messageInterface.reply(Function.FunctionQueryResponse.class, id,
                  General.Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
                      .build());
            } else if (request.getFunctionId() == null || request.getFunctionId().isEmpty()) {
              List<FaasEntities.Function> functions = functionDomainRepository.findAll(request.getUserId()).stream()
                  .map(FUNCTION_CONVERTER::applyBack).collect(Collectors.toList());
              FunctionQueryResponse response = FunctionQueryResponse.newBuilder()
                  .addAllFunctions(functions).build();
              messageInterface.reply(id, response);
            } else {
              FaasEntities.Function function = FUNCTION_CONVERTER.applyBack(functionDomainRepository
                  .findByIdAndTenant(request.getFunctionId(), request.getUserId()));
              FunctionQueryResponse response = FunctionQueryResponse.newBuilder()
                  .addFunctions(function).build();
              messageInterface.reply(id, response);
            }
          } catch (Exception e) {
            LOGGER.error("Caught exception {} during execution of {}", e.getMessage(), this, e);
            messageInterface.reply(FunctionQueryResponse.class, id,
                General.Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
          }
        });
  }
}
