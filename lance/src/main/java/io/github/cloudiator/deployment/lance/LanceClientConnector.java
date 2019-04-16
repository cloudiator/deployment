/*
 * Copyright 2014-2018 University of Ulm
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

package io.github.cloudiator.deployment.lance;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;

public class LanceClientConnector {

  @Inject(optional = true)
  @Named("lance.rmiTimeout")
  private int rmiTimeout = 0;

  @Inject
  public LanceClientConnector() {
  }

  LifecycleClient getLifecycleClient(String serverIp) throws IOException {
    return getLifecycleClient(serverIp, rmiTimeout, true);
  }

  LifecycleClient getLifecycleClient(String serverIp, int rmiTimeout, boolean retry)
      throws IOException {

    Callable<LifecycleClient> createLifecycleClient = new Callable<LifecycleClient>() {
      public LifecycleClient call() throws Exception {
        final LifecycleClient lifecycleClient = LifecycleClient
            .getClient(serverIp, rmiTimeout);

        return lifecycleClient;
      }
    };

    if (!retry) {
      try {
        return createLifecycleClient.call();
      } catch (Exception e) {
        throw new IOException("Error connecting to lifecycle agent.", e);
      }
    }

    Retryer<LifecycleClient> retryer = RetryerBuilder.<LifecycleClient>newBuilder()
        .retryIfResult(Predicates.<LifecycleClient>isNull())
        .retryIfExceptionOfType(RemoteException.class)
        .retryIfExceptionOfType(NotBoundException.class)
        .retryIfExceptionOfType(ConnectException.class)
        .withWaitStrategy(WaitStrategies.fixedWait(20, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(5))
        .build();

    final LifecycleClient lifecycleClient;

    try {
      lifecycleClient = retryer.call(createLifecycleClient);

    } catch (ExecutionException e) {
      throw new IOException(
          "Could not connect to lifecycle client", e.getCause());
    } catch (RetryException e) {
      throw new IOException("Error creating lifecycle client! Exceeded retry attempts!",
          e);
    }
    return lifecycleClient;
  }

}
