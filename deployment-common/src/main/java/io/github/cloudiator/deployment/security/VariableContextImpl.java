/*
 * Copyright 2014-2019 University of Ulm
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

package io.github.cloudiator.deployment.security;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cloudiator.messages.entities.SecureStore.SecureStoreRetrieveRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.SecureStoreService;

public class VariableContextImpl implements VariableContext {

  private static final Pattern PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

  private final String userId;
  private final SecureStoreService secureStoreService;

  @Inject
  public VariableContextImpl(
      @Assisted String userId, SecureStoreService secureStoreService) {
    this.userId = userId;
    this.secureStoreService = secureStoreService;
  }

  @Override
  public String parse(String string) {

    if (string == null) {
      return null;
    }

    final Matcher m = PATTERN.matcher(string);
    final StringBuffer b = new StringBuffer(string.length());
    while (m.find()) {
      m.appendReplacement(b, retrieve(m.group(1).trim()));
    }
    m.appendTail(b);

    return b.toString();
  }

  @Override
  public String retrieve(String key) {

    checkNotNull(key);

    Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
        .retryIfExceptionOfType(ResponseException.class)
        .withWaitStrategy(WaitStrategies.fixedWait(30, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(5))
        .build();

    try {
      return retryer.call(() -> {
        final String value = secureStoreService.retrieveSecret(
            SecureStoreRetrieveRequest.newBuilder().setUserId(userId).setKey(key).build())
            .getValue();
        checkState(!Strings.isNullOrEmpty(value),
            String.format("Retrieved value for variable %s is null or empty.", key));
        return value;
      });
    } catch (ExecutionException e) {
      throw new VariableReplacementException("Could not retrieve value for variable " + key,
          e.getCause());
    } catch (RetryException e) {
      throw new VariableReplacementException("Could not retrieve value for variable " + key, e);
    }
  }
}
