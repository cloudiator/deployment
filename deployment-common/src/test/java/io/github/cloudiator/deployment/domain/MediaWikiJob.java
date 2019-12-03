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

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemImpl;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import java.util.UUID;
import org.cloudiator.matchmaking.domain.OclRequirement;

public class MediaWikiJob {

  private static final String JOB_ID = UUID.randomUUID().toString();
  public static final OperatingSystem OS = new OperatingSystemImpl(OperatingSystemFamily.UBUNTU,
      OperatingSystemArchitecture.AMD64, OperatingSystemVersions.ofNameAndVersion(1404, "14.04"));

  private MediaWikiJob() {
    throw new AssertionError("Do not instantiate");
  }

  public static PortRequired wikiRequiresDatabase() {
    return PortRequiredBuilder.newBuilder()
        .name("WIKIREQMARIADB")
        .isMandatory(true).build();
  }

  public static PortProvided wikiProvided() {
    return PortProvidedBuilder.newBuilder().name("WIKIPROV").port(80)
        .build();
  }

  public static Task wikiTask() {

    final LanceInterface wikiInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.DOCKER).os(OS).preInstall("./preInstall")
        .install("./install").postInstall("./postInstall").start("./start").build();

    final OclRequirement wikiRequirementCores = OclRequirement
        .of("nodes->forAll(hardware.cores >= 2)");
    final OclRequirement wikiRequirementRam = OclRequirement
        .of("nodes->forAll(hardware.ram >= 2048)");
    final OclRequirement wikiRequirementImage = OclRequirement
        .of("nodes->forAll(image.providerId = '83f41918-2d73-49dd-8f42-4983fbdbf705')");

    return TaskBuilder.newBuilder().name("wiki").addPort(wikiProvided())
        .addPort(wikiRequiresDatabase())
        .addInterface(wikiInterface)
        .addRequirement(wikiRequirementCores).addRequirement(wikiRequirementRam)
        .addRequirement(wikiRequirementImage).behaviour(service()).build();

  }

  public static PortProvided databaseProvided() {
    return PortProvidedBuilder.newBuilder().name("MARIADBPROV")
        .port(3306).build();
  }

  public static Task databaseTask() {

    final LanceInterface databaseInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.DOCKER).os(OS)
        .preInstall("./preInstall").install("/.install")
        .postInstall("./postInstall").start("./start").build();

    final OclRequirement databaseRequirementCores = OclRequirement
        .of("nodes->forAll(hardware.cores >= 4)");
    final OclRequirement databaseRequirementRam = OclRequirement
        .of("nodes->forAll(hardware.ram >= 4096)");
    final OclRequirement databaseRequirementImage = OclRequirement
        .of("nodes->forAll(image.providerId = '83f41918-2d73-49dd-8f42-4983fbdbf705')");

    return TaskBuilder.newBuilder().name("database").addPort(databaseProvided())
        .addInterface(databaseInterface).addRequirement(databaseRequirementCores)
        .addRequirement(databaseRequirementImage).addRequirement(databaseRequirementRam)
        .behaviour(service()).build();

  }

  public static PortProvided lbProv() {
    return PortProvidedBuilder.newBuilder().name("LBPROV").port(80).build();
  }

  public static PortRequired loadbalancerreqwiki() {
    return PortRequiredBuilder.newBuilder()
        .name("LOADBALANCERREQWIKI").isMandatory(false).build();
  }

  public static Task loadBalancerTask() {

    final LanceInterface lbInterface = LanceInterfaceBuilder.newBuilder()
        .containerType(LanceContainerType.DOCKER).os(OS).preInstall("./preInstall")
        .install("./install").start("./start").portUpdateAction("./updateAction").build();

    final OclRequirement lbRequirementCores = OclRequirement
        .of("nodes->forAll(hardware.cores >= 1)");
    final OclRequirement lbRequirementRam = OclRequirement
        .of("nodes->forAll(hardware.ram >= 1024)");
    final OclRequirement lbRequirementImage = OclRequirement
        .of("nodes->forAll(image.providerId = '83f41918-2d73-49dd-8f42-4983fbdbf705')");

    return TaskBuilder.newBuilder().name("loadbalancer").addPort(lbProv())
        .addPort(loadbalancerreqwiki())
        .addInterface(lbInterface).addRequirement(lbRequirementCores)
        .addRequirement(lbRequirementRam).addRequirement(lbRequirementImage).behaviour(service())
        .build();
  }

  public static Communication wikiWithLB() {
    return CommunicationBuilder.newBuilder().portProvided("WIKIPROV")
        .portRequired("LOADBALANCERREQWIKI").build();
  }

  public static Communication wikiWithDB() {
    return CommunicationBuilder.newBuilder().portProvided("MARIADBPROV")
        .portRequired("WIKIREQMARIADB").build();
  }

  public static Job wikiJob() {

    return JobBuilder.newBuilder().id(JOB_ID).userId("admin").name("mediawiki")
        .addTask(wikiTask()).addTask(databaseTask()).addTask(loadBalancerTask())
        .addCommunication(wikiWithDB()).addCommunication(wikiWithLB())
        .build();
  }

  public static Behaviour service() {
    return Behaviours.service(true);
  }


}
