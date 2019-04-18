/*
 * Copyright 2018 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.scheduler.messaging;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.CloudiatorProcess;
import io.github.cloudiator.deployment.messaging.ProcessMessageConverter;
import io.github.cloudiator.persistance.ProcessDomainRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.ProcessService;

public class ProcessQuerySubscriber implements Runnable {

  private static final ProcessMessageConverter PROCESS_MESSAGE_CONVERTER = ProcessMessageConverter.INSTANCE;

  private final ProcessService processService;
  private final ProcessDomainRepository processDomainRepository;
  private final MessageInterface messageInterface;


  @Inject
  public ProcessQuerySubscriber(ProcessService processService,
      ProcessDomainRepository processDomainRepository,
      MessageInterface messageInterface) {
    this.processService = processService;
    this.processDomainRepository = processDomainRepository;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    processService.subscribeProcessQueryRequest(new MessageCallback<ProcessQueryRequest>() {
      @Override
      public void accept(String id, ProcessQueryRequest content) {

        final String userId = Strings.emptyToNull(content.getUserId());
        final String scheduleId = Strings.emptyToNull(content.getScheduleId());
        final String processId = Strings.emptyToNull(content.getProcessId());

        try {
          final List<CloudiatorProcess> query = query(userId, scheduleId, processId);

          messageInterface.reply(id, ProcessQueryResponse.newBuilder()
              .addAllProcesses(query.stream().map(PROCESS_MESSAGE_CONVERTER::applyBack).collect(
                  Collectors.toList())).build());


        } catch (Exception e) {
          messageInterface.reply(ProcessQueryResponse.class, id, Error.newBuilder().setCode(500)
              .setMessage("Error while querying processes: " + e.getMessage()).build());
        }
      }
    });
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  List<CloudiatorProcess> query(String userId, String scheduleId, String processId) {

    if (userId == null) {
      throw new IllegalStateException("UserId is null or empty");
    }

    if (scheduleId != null && processId != null) {
      throw new IllegalStateException("ScheduleId and ProcessId are set");
    }

    if (scheduleId == null && processId != null) {
      //query by process
      return Collections.singletonList(processDomainRepository.getByIdAndUser(processId, userId));
    }

    if (scheduleId != null && processId == null) {
      //query by schedule
      return processDomainRepository.getByScheduleIdAndUser(scheduleId, userId);
    }

    //query all by user
    return processDomainRepository.getByUser(userId);
  }


}
