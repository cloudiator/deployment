package io.github.cloudiator.deployment.lance;

import static org.junit.Assert.assertEquals;

import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.DockerInterfaceImpl;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import io.github.cloudiator.deployment.lance.DockerComponentSupplier.DockerCredentials;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class PublicPrivateDistinguishTest {

  private static Task taskSimplePub, taskExtendedPub, taskPrivateWork, taskPrivateNotWork1, taskPrivateNotWork2;
  private static Job jobSimplePub, jobExtendedPub, jobPrivateWork, jobPrivateNotWork1, jobPrivateNotWork2;
  private static String hostName;
  private static int port;
  private static String imageNameSpace, imageName;
  private static String tag;
  private static String taskPubSimpleName, taskPubExtendedName, taskPrivateWorkName, taskPrivateNotWorkName1, taskPrivateNotWorkName2;
  private static String jobPubSimpleName, jobPubExtendedName, jobPrivateWorkName, jobPrivateNotWorkName1, jobPrivateNotWorkName2;
  private static String absoluteImageNameSimplePublic, absoluteImageNameExtendedPublic, absoluteImageNamePrivate;
  private static String username, password;
  private static Map<String,String> envMapPrivateWork, envMapPrivateNotWork1, envMapPrivateNotWork2;
  private static DockerInterface dIfaceSimplePub, dIfaceExtendedPub, dIfacePrivateWork, dIfacePrivateNotWork1, dIfacePrivateNotWork2;
  private static DockerComponentSupplier componentSimplePubSuppl, componentExtendedPubSuppl;
  private static PrivateDockerComponentSupplier componentPrivateWorkSuppl, componentPrivateNotWorkSuppl1, componentPrivateNotWorkSuppl2;

  @BeforeClass
  public static void setup() {
    setupStringFields();
    setupIfaces();

    taskSimplePub = TaskBuilder.newBuilder().name(taskPubSimpleName).addPorts(new ArrayList<>())
        .addInterface(dIfaceSimplePub).addRequirements(new ArrayList<>()).optimization(null).build();
    taskExtendedPub = TaskBuilder.newBuilder().name(taskPubExtendedName).addPorts(new ArrayList<>())
        .addInterface(dIfaceExtendedPub).addRequirements(new ArrayList<>()).optimization(null).build();
    taskPrivateWork = TaskBuilder.newBuilder().name(taskPrivateWorkName).addPorts(new ArrayList<>())
        .addInterface(dIfacePrivateWork).addRequirements(new ArrayList<>()).optimization(null).build();
    taskPrivateNotWork1 = TaskBuilder.newBuilder().name(taskPrivateNotWorkName1).addPorts(new ArrayList<>())
        .addInterface(dIfacePrivateNotWork1).addRequirements(new ArrayList<>()).optimization(null).build();
    taskPrivateNotWork2 = TaskBuilder.newBuilder().name(taskPrivateNotWorkName2).addPorts(new ArrayList<>())
        .addInterface(dIfacePrivateNotWork2).addRequirements(new ArrayList<>()).optimization(null).build();

    jobSimplePub = JobBuilder.newBuilder().generateId().name(jobPubSimpleName)
        .addCommunications(new HashSet<>()).addTask(taskSimplePub).build();
    jobExtendedPub = JobBuilder.newBuilder().generateId().name(jobPubExtendedName)
        .addCommunications(new HashSet<>()).addTask(taskExtendedPub).build();
    jobPrivateWork = JobBuilder.newBuilder().generateId().name(jobPrivateWorkName)
        .addCommunications(new HashSet<>()).addTask(taskPrivateWork).build();
    jobPrivateNotWork1 = JobBuilder.newBuilder().generateId().name(jobPrivateNotWorkName1)
        .addCommunications(new HashSet<>()).addTask(taskPrivateNotWork1).build();
    jobPrivateNotWork2 = JobBuilder.newBuilder().generateId().name(jobPrivateNotWorkName2)
        .addCommunications(new HashSet<>()).addTask(taskPrivateNotWork2).build();


    componentSimplePubSuppl = new PublicDockerComponentSupplier(jobSimplePub, taskSimplePub);
    componentExtendedPubSuppl = new PublicDockerComponentSupplier(jobExtendedPub, taskExtendedPub);
    componentPrivateWorkSuppl = new PrivateDockerComponentSupplier(jobPrivateWork, taskPrivateWork);
    componentPrivateNotWorkSuppl1 = new PrivateDockerComponentSupplier(jobPrivateNotWork1, taskPrivateNotWork1);
    componentPrivateNotWorkSuppl2 = new PrivateDockerComponentSupplier(jobPrivateNotWork2, taskPrivateNotWork2);
  }

  private static void setupIfaces() {
    dIfaceSimplePub = new DockerInterfaceImpl(absoluteImageNameSimplePublic, new HashMap<>());
    dIfaceExtendedPub = new DockerInterfaceImpl(absoluteImageNameExtendedPublic, new HashMap<>());
    dIfacePrivateWork = new DockerInterfaceImpl(absoluteImageNamePrivate, envMapPrivateWork);
    dIfacePrivateNotWork1 = new DockerInterfaceImpl(absoluteImageNamePrivate, envMapPrivateNotWork1);
    dIfacePrivateNotWork2 = new DockerInterfaceImpl(absoluteImageNamePrivate, envMapPrivateNotWork2);
  }

  private static void setupStringFields() {
    taskPubSimpleName = "taskPubSimple";
    taskPubExtendedName = "taskPubExtended";
    taskPrivateWorkName = "taskPrivateWork";
    taskPrivateNotWorkName1 = "taskPrivateNotWork1";
    taskPrivateNotWorkName2 = "taskPrivateNotWork2";
    jobPubSimpleName = "jobPubSimple";
    jobPubExtendedName = "jobPubExtended";
    jobPrivateWorkName = "jobPrivateWork";
    jobPrivateNotWorkName1 = "jobPrivateNotWork1";
    jobPrivateNotWorkName2 = "jobPrivateNotWork2";
    hostName = "my-local-reg.de";
    port = 443;
    imageNameSpace = "namespace";
    imageName = "dockerImage";
    tag = "latest";
    absoluteImageNameSimplePublic = imageName + ":" + tag;
    absoluteImageNameExtendedPublic = hostName + ":" + Integer.toString(port) + "/" + imageNameSpace + "/" + imageName + ":" + tag;
    absoluteImageNamePrivate = absoluteImageNameExtendedPublic;
    username = "john-doe";
    password = "top-secret";
    envMapPrivateWork = new HashMap<>();
    envMapPrivateWork.put("username", username);
    envMapPrivateWork.put("password", password);
    envMapPrivateNotWork1 = new HashMap<>();
    envMapPrivateNotWork1.put("username", username);
    envMapPrivateNotWork2 = new HashMap<>();
    envMapPrivateNotWork2.put("password", password);
  }

  @Test
  public void testIsPublicSimple() {
    final DockerCredentials creds = componentSimplePubSuppl.getCredentials();
    final boolean usePrivReg = componentSimplePubSuppl.usePrivateRegistry(dIfaceSimplePub);

    assertEquals("", creds.username);
    assertEquals("", creds.password);
    assertEquals(false, usePrivReg);
  }

  @Test
  public void testIsPublicExtended() {
    final DockerCredentials creds = componentExtendedPubSuppl.getCredentials();
    final boolean usePrivReg= componentExtendedPubSuppl.usePrivateRegistry(dIfaceExtendedPub);

    assertEquals("", creds.username);
    assertEquals("", creds.password);
    assertEquals(true, usePrivReg);
  }

  private void assertPublic(DockerCredentials creds, boolean shldHaveNoCreds) {
    assertEquals("", creds.username);
    assertEquals("", creds.password);
    assertEquals(false, shldHaveNoCreds);
  }

  @Test
  public void testIsPrivateWork() {
    final DockerCredentials creds = componentPrivateWorkSuppl.getCredentials();
    final boolean hasCreds = componentPrivateWorkSuppl.usePrivateRegistry(dIfacePrivateWork);

    assertEquals(username, creds.username);
    assertEquals(password, creds.password);
    assertEquals(true, hasCreds);
  }

  @Test
  public void testIsPrivateNotWork1() {
    final DockerCredentials creds = componentPrivateNotWorkSuppl1.getCredentials();
    final boolean hasCreds = componentPrivateNotWorkSuppl1.usePrivateRegistry(dIfacePrivateNotWork1);

    assertEquals(username, creds.username);
    assertEquals("", creds.password);
    assertEquals(true, hasCreds);
  }

  @Test
  public void testIsPrivateNotWork2() {
    final DockerCredentials creds = componentPrivateNotWorkSuppl2.getCredentials();
    final boolean hasCreds = componentPrivateNotWorkSuppl2.usePrivateRegistry(dIfacePrivateNotWork2);

    assertEquals("", creds.username);
    assertEquals(password, creds.password);
    assertEquals(true, hasCreds);
  }
}
