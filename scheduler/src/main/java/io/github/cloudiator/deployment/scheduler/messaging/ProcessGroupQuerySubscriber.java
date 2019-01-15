package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.ProcessGroup;
import io.github.cloudiator.deployment.messaging.ProcessGroupMessageConverter;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ProcessGroupQueryMessage;
import org.cloudiator.messages.Process.ProcessGroupQueryResponse;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 29.12.2018.
 */
public class ProcessGroupQuerySubscriber implements Runnable  {

  private static final ProcessGroupMessageConverter PROCESS_GROUP_MESSAGE_CONVERTER = new ProcessGroupMessageConverter();

  private final ProcessService processService;
  private final ProcessDomainRepository processDomainRepository;

  private final MessageInterface messageInterface;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProcessGroupQuerySubscriber.class);


  @Inject
  public ProcessGroupQuerySubscriber(ProcessService processService,
      ProcessDomainRepository processDomainRepository,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.processDomainRepository = processDomainRepository;
    this.messageInterface = messageInterface;
  }


  @Override
  public void run() {

    this.processService.subscribeProcessGroupQueryRequest(new MessageCallback<ProcessGroupQueryMessage>() {
      @Override
      public void accept(String id, ProcessGroupQueryMessage content) {

        final String userId = Strings.emptyToNull(content.getUserId());
        final String processGroupId = Strings.emptyToNull(content.getProcessGroupId());

        try {

          final List<ProcessGroup> query = query(userId, processGroupId);

          messageInterface.reply(id, ProcessGroupQueryResponse.newBuilder()
              .addAllProcessGroups(query.stream().map(PROCESS_GROUP_MESSAGE_CONVERTER::applyBack).collect(
                  Collectors.toList())).build());

        } catch (Exception e) {
          LOGGER.error("Error while fetching processGroups!",e);
          messageInterface.reply(ProcessQueryResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage("Error while querying processGroups: " + e.getMessage()).build());
        }
      }
    });
  }

  @Transactional
  List<ProcessGroup> query(String userId, String processGroupId) {

    if (userId == null) {
      throw new IllegalStateException("UserId is null or empty");
    }

    if (processGroupId != null) {
      //query by processGroupId
      return   Collections.singletonList(processDomainRepository.findGroupByTenantAndId(userId,processGroupId));
    }


    //query all by user
    return processDomainRepository.findGroupsByTenant(userId);
  }


}
