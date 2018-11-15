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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.lance.client.LifecycleClient;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.inject.Named;

public class LanceClientConnector {

  @Inject(optional = true)
  @Named("lance.rmiTimeout")
  private int rmiTimeout = 0;

  @Inject
  public LanceClientConnector() {
  }

  LifecycleClient getLifecycleClient(String serverIp) {
    final LifecycleClient lifecycleClient;
    try {
      lifecycleClient = LifecycleClient
          .getClient(serverIp, rmiTimeout);
    } catch (RemoteException | NotBoundException e) {
      throw new IllegalStateException("Error creating lifecycle client", e);
    }
    return lifecycleClient;
  }

}
