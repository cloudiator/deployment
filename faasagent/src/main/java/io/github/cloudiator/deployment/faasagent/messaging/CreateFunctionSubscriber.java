package io.github.cloudiator.deployment.faasagent.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.deployment.domain.FunctionBuilder;
import io.github.cloudiator.deployment.faasagent.helper.SaveFunctionHelper;
import io.github.cloudiator.deployment.messaging.FunctionConverter;
import io.github.cloudiator.messaging.RuntimeConverter;
import org.cloudiator.messages.Function.CreateFunctionRequestMessage;
import org.cloudiator.messages.Function.FunctionCreatedResponse;
import org.cloudiator.messages.General;
import org.cloudiator.messages.entities.FaasEntities;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateFunctionSubscriber implements Runnable {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CreateFunctionSubscriber.class);
  private final MessageInterface messageInterface;
  private final SaveFunctionHelper saveFunctionHelper;
  private final FunctionConverter functionConverter = new FunctionConverter();
  private final RuntimeConverter runtimeConverter = RuntimeConverter.INSTANCE;

  @Inject
  public CreateFunctionSubscriber(MessageInterface messageInterface,
                                  SaveFunctionHelper saveFunctionHelper) {
    this.messageInterface = messageInterface;
    this.saveFunctionHelper = saveFunctionHelper;
  }

  @Override
  public void run() {
    messageInterface.subscribe(CreateFunctionRequestMessage.class,
        CreateFunctionRequestMessage.parser(),
        (requestId, request) -> {
          try {
            String userId = request.getUserId();
            FaasEntities.FunctionRequest functionRequest = request.getFunctionRequest();

            Function function = createFunction(functionRequest);

            saveFunctionHelper.persistFunction(function, userId);

            FunctionCreatedResponse functionCreatedResponse = FunctionCreatedResponse.newBuilder()
                .setFunction(functionConverter.applyBack(function)).build();

            messageInterface.reply(requestId, functionCreatedResponse);
          } catch (Exception e) {
            LOGGER.error("Failed to create funtion", e);
            messageInterface.reply(FunctionCreatedResponse.class, requestId,
                General.Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
          }
        });
  }

  private Function createFunction(FaasEntities.FunctionRequest request) {
    return FunctionBuilder.newBuilder()
        .generateId()
        .cloudId(request.getCloudId())
        .locationId(request.getLocationId())
        .memory(request.getMemory())
        .runtime(runtimeConverter.apply(request.getRuntime()))
        .stackId("")
        .build();
  }

}
