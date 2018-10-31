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

package io.github.cloudiator.deployment.yaml.messaging;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Schedule;
import io.github.cloudiator.deployment.messaging.ScheduleConverter;
import io.github.cloudiator.deployment.yaml.YAMLModelInstantiation;
import io.github.cloudiator.deployment.yaml.YAMLModelInstantiation.YAMLModelInstantiationFactory;
import io.github.cloudiator.deployment.yaml.YAMLParser;
import io.github.cloudiator.deployment.yaml.model.YAMLModel;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Job.YAMLRequest;
import org.cloudiator.messages.Job.YAMLResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YAMLRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(YAMLRequestSubscriber.class);

  private final MessageInterface messageInterface;
  private final YAMLParser yamlParser;
  private final YAMLModelInstantiation.YAMLModelInstantiationFactory yamlModelInstantiationFactory;

  @Inject
  public YAMLRequestSubscriber(MessageInterface messageInterface,
      YAMLParser yamlParser,
      YAMLModelInstantiationFactory yamlModelInstantiationFactory) {
    this.messageInterface = messageInterface;
    this.yamlParser = yamlParser;
    this.yamlModelInstantiationFactory = yamlModelInstantiationFactory;
  }


  @Override
  public void run() {
    messageInterface.subscribe(YAMLRequest.class, YAMLRequest.parser(),
        new MessageCallback<YAMLRequest>() {
          @Override
          public void accept(String id, YAMLRequest content) {

            final String userId = content.getUserId();

            try {

              final YAMLModel parse = yamlParser.parse(content.getYaml());
              final Schedule instantiate = yamlModelInstantiationFactory.create(parse, userId)
                  .instantiate();

              messageInterface.reply(id, YAMLResponse.newBuilder()
                  .setSchedule(ScheduleConverter.INSTANCE.applyBack(instantiate)).build());

            } catch (JsonParseException | JsonMappingException e) {
              LOGGER.error("Encountered invalid YAML: " + e.getMessage(), e);
              messageInterface.reply(YAMLResponse.class, id,
                  Error.newBuilder().setCode(400).setMessage("Invalid YAML: " + e.getMessage())
                      .build());
            } catch (Exception e) {
              LOGGER.error(String
                  .format("Unexpected exception while processing request %s: %s", content,
                      e.getMessage()), e);
            }

          }
        });
  }
}
