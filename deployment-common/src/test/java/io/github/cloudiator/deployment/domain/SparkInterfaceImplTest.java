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

package io.github.cloudiator.deployment.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import org.junit.Test;

public class SparkInterfaceImplTest {

  @Test
  public void decorateEnvironment() {

    final SparkInterface sparkInterface = SparkInterfaceBuilder.newBuilder().processMapping(ProcessMapping.CLUSTER).file("file")
        .className("de.uniulm.App").build();

    Environment environment = new Environment();
    environment.put("PUBLIC_PORT", "http://example.com");

    final SparkInterface decoratedInterface = (SparkInterface) sparkInterface
        .decorateEnvironment(environment);

    assertThat(decoratedInterface.arguments(), contains("--PUBLIC_PORT", "http://example.com"));


  }
}
