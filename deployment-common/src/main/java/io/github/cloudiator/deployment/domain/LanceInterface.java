/*
 * Copyright 2017 University of Ulm
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

package io.github.cloudiator.deployment.domain;

import java.util.Optional;

/**
 * Created by daniel on 13.02.17.
 */
public interface LanceInterface extends TaskInterface {

  LanceContainerType containerType();

  Optional<String> init();

  Optional<String> preInstall();

  Optional<String> install();

  Optional<String> postInstall();

  Optional<String> preStart();

  String start();

  Optional<String> startDetection();

  Optional<String> stopDetection();

  Optional<String> postStart();

  Optional<String> preStop();

  Optional<String> stop();

  Optional<String> postStop();

  Optional<String> shutdown();

  Optional<String> portUpdateAction();
}
