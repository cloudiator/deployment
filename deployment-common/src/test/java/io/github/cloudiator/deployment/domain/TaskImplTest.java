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

package io.github.cloudiator.deployment.domain;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

public class TaskImplTest {

  @Test
  public void interfaceOfType() {

    LanceInterface lanceInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.BOTH).start("./start.sh").build();

    Task task = TaskBuilder.newBuilder().addInterface(lanceInterface).name("test")
        .behaviour(Behaviours.service(true)).build();

    assertThat(task.interfaceOfType(LanceInterface.class), equalTo(lanceInterface));

  }

  @Test(expected = IllegalArgumentException.class)
  public void interfaceOfTypeDoesNotExist() {
    Task task = TaskBuilder.newBuilder().name("test").behaviour(Behaviours.service(true)).build();
    task.interfaceOfType(LanceInterface.class);
  }

  @Test
  public void providedAndRequiredPorts() {
    LanceInterface lanceInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.BOTH).start("./start.sh").build();

    PortProvided provided = PortProvidedBuilder.newBuilder().name("PortProvided").port(1234)
        .build();
    PortRequired required = PortRequiredBuilder.newBuilder().name("PortRequired").isMandatory(true)
        .build();

    Task task = TaskBuilder.newBuilder().addInterface(lanceInterface).name("providedPortTests")
        .addPort(provided).addPort(required).behaviour(Behaviours.service(true))
        .build();

    assertThat(task.providedPorts(), contains(provided));
    assertThat(task.providedPorts(), not(contains(required)));

    assertThat(task.requiredPorts(), contains(required));
    assertThat(task.requiredPorts(), not(contains(provided)));


  }

  @Test
  public void equals() {

    LanceInterface lanceInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.BOTH).start("./start.sh").build();

    PortProvided provided = PortProvidedBuilder.newBuilder().name("PortProvided").port(1234)
        .build();
    PortRequired required = PortRequiredBuilder.newBuilder().name("PortRequired").isMandatory(true)
        .build();

    Task thisTask = TaskBuilder.newBuilder().addInterface(lanceInterface).name("providedPortTests")
        .addPort(provided).addPort(required).behaviour(Behaviours.service(true))
        .build();

    Task thatTask = TaskBuilder.newBuilder().addInterface(lanceInterface).name("providedPortTests")
        .addPort(provided).addPort(required).behaviour(Behaviours.service(true))
        .build();

    assertThat(thisTask, equalTo(thatTask));
    assertThat(thatTask, equalTo(thisTask));


  }
}
