package io.github.cloudiator.deployment.lance;

import static org.junit.Assert.assertEquals;

import de.uniulm.omi.cloudiator.lance.application.component.RemoteDockerComponent;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommand;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommand.Type;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommandException;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.EntireDockerCommands;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.DockerInterfaceImpl;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class PrivateDockerComponentSupplierTest {

  private static String hostName;
  private static int port;
  private static String imageNameSpace, imageName;
  private static String tag;
  private static String absoluteImageNameNoPort, absoluteImageNamePort;
  private static String username, password;
  private static String taskNoCredName, taskCredName;
  private static String jobNoCredName, jobCredName;
  private static String dockerCreateOptionsString, dockerCreateOsCommandString, dockerCreateArgsString;
  private static String dockerStartOptionsString;
  private static Map<String,String> envMapNoCred, envMapCred;
  private static Task taskNoCred, taskCred;
  private static Job jobNoCred, jobCred;
  private static RemoteDockerComponent componentNoCred, componentCred;

  @BeforeClass
  public static void setup() {
    setupStringFields();

    //no credentials, no port
    DockerInterface dIfaceNoCred = new DockerInterfaceImpl(absoluteImageNameNoPort, envMapNoCred);
    //credentials and port
    DockerInterface dIfaceCred = new DockerInterfaceImpl(absoluteImageNamePort, envMapCred);

    taskNoCred = TaskBuilder.newBuilder().name(taskNoCredName).addPorts(new ArrayList<>())
        .addInterface(dIfaceNoCred).addRequirements(new ArrayList<>()).optimization(null).build();
    taskCred = TaskBuilder.newBuilder().name(taskCredName).addPorts(new ArrayList<>())
        .addInterface(dIfaceCred).addRequirements(new ArrayList<>()).optimization(null).build();

    jobNoCred = JobBuilder.newBuilder().generateId().name(jobNoCredName)
        .addCommunications(new HashSet<>()).addTask(taskNoCred).build();
    jobCred = JobBuilder.newBuilder().generateId().name(jobCredName)
        .addCommunications(new HashSet<>()).addTask(taskCred).build();


    componentNoCred = new PrivateDockerComponentSupplier(jobNoCred, taskNoCred).get();
    componentCred = new PrivateDockerComponentSupplier(jobCred, taskCred).get();
  }

  private static void setupStringFields() {
    taskNoCredName = "taskNoCred";
    taskCredName = "taskCred";
    jobNoCredName = "jobNoCred";
    jobCredName = "jobCred";
    hostName = "my-local-reg.de";
    port = 443;
    imageNameSpace = "namespace";
    imageName = "dockerImage";
    tag = "latest";
    absoluteImageNameNoPort = hostName + "/" + imageNameSpace + "/" + imageName + ":" + tag;
    absoluteImageNamePort = hostName + ":" + Integer.toString(port) + "/" + imageNameSpace + "/" + imageName + ":" + tag;
    username = "john-doe";
    password = "top-secret";
    envMapNoCred = new HashMap<>();
    envMapNoCred.put("foo","bar");
    envMapNoCred.put("john","doe");
    envMapCred = new HashMap<>();
    envMapCred.putAll(envMapNoCred);
    envMapCred.put("username", username);
    envMapCred.put("password", password);
    dockerCreateOptionsString = "--env foo=bar --env john=doe --interactive  --restart no";
    dockerCreateOsCommandString = "bash";
    dockerCreateArgsString = "--noediting";
    dockerStartOptionsString = "--interactive";
  }

  @Test
  public void testDockerCreateOptions() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();

    testDockerOptions(cmdsNoCred, Type.CREATE, dockerCreateOptionsString);
    testDockerOptions(cmdsCred, Type.CREATE, dockerCreateOptionsString);
  }

  @Test
  public void testDockerStartOptions() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();

    testDockerOptions(cmdsNoCred, Type.START, dockerStartOptionsString);
    testDockerOptions(cmdsCred, Type.START, dockerStartOptionsString);
  }

  private void testDockerOptions(EntireDockerCommands cmds, DockerCommand.Type dCType, String optionsString) {
    String setOptionsString;
    try {
      setOptionsString = cmds.getSetOptionsString(dCType).trim();
      assertEquals(setOptionsString, optionsString);
    } catch (DockerCommandException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testDockerCreateOsCommand() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();

    testDockerOsCommand(cmdsNoCred, Type.CREATE, dockerCreateOsCommandString);
    testDockerOsCommand(cmdsCred, Type.CREATE, dockerCreateOsCommandString);
  }

  private void testDockerOsCommand(EntireDockerCommands cmds, DockerCommand.Type dCType, String osCommandString) {
    String setOsCommandString;
    try {
      setOsCommandString = cmds.getSetOsCommandString(dCType).trim();
      assertEquals(setOsCommandString, osCommandString);
    } catch (DockerCommandException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testDockerCreateArgs() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();

    testDockerArgs(cmdsNoCred, Type.CREATE, dockerCreateArgsString);
    testDockerArgs(cmdsCred, Type.CREATE, dockerCreateArgsString);
  }

  private void testDockerArgs(EntireDockerCommands cmds, DockerCommand.Type dCType, String argsString) {
    String setArgsString;
    try {
      setArgsString = cmds.getSetArgsString(dCType).trim();
      assertEquals(setArgsString, argsString);
    } catch (DockerCommandException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testImageNameSpace() {
    final String noCredNameSpace = componentNoCred.getImageFolder().trim();
    final String credNameSpace = componentCred.getImageFolder().trim();

    assertEquals(noCredNameSpace, imageNameSpace);
    assertEquals(credNameSpace, imageNameSpace);
  }

  @Test
  public void testImageName() {
    final String noCredImageName = componentNoCred.getImageName().trim();
    final String credImageName = componentCred.getImageName().trim();

    assertEquals(noCredImageName, imageName);
    assertEquals(credImageName, imageName);
  }

  @Test
  public void testTag() {
    final String noCredTag = componentNoCred.getTag().trim();
    final String credTag = componentCred.getTag().trim();

    assertEquals(noCredTag, tag);
    assertEquals(credTag, tag);
  }

  @Test
  public void testFullImageName() {
    final String fullImageNameNoCred = componentNoCred.getFullImageName();
    final String fullImageNameCred = componentCred.getFullImageName();

    //no credentials, no port
    assertEquals(fullImageNameNoCred, absoluteImageNameNoPort);
    //credentials and port
    assertEquals(fullImageNameCred, absoluteImageNamePort);
  }

  @Test
  public void testHostName() {
    final String uriEndPointNoCred = componentNoCred.getUriEndPoint();
    final String uriEndPointCred = componentCred.getUriEndPoint();

    final String hostNameNoCred = uriEndPointNoCred.split(":")[0];
    final String hostNameCred = uriEndPointCred.split(":")[0];

    assertEquals(hostNameNoCred, hostName);
    assertEquals(hostNameCred, hostName);
  }

  @Test
  public void testPort() {
    final int getPortNoCred = componentNoCred.getDockerReg().port;
    final String uriEndPointNoCred = componentNoCred.getUriEndPoint();
    //no port
    final List<String> uriSplitNoCred = new ArrayList<>(Arrays.asList(uriEndPointNoCred.split(":")));
    final int getPortCred = componentCred.getDockerReg().port;
    final String uriEndPointCred = componentCred.getUriEndPoint();
    //port
    final List<String> uriSplitCred = new ArrayList<>(Arrays.asList(uriEndPointCred.split(":")));

    //no port
    assertEquals(getPortNoCred,-1);
    //port
    assertEquals(getPortCred,port);
    //no port
    assertEquals(uriSplitNoCred.size(), 1);
    //port
    assertEquals(uriSplitCred.size(), 2);
    //port
    assertEquals(Integer.parseInt(uriSplitCred.get(1)), port);
  }

  @Test
  public void testCredentials() {
    final RemoteDockerComponent.DockerRegistry regNoCred = componentNoCred.getDockerReg();
    final RemoteDockerComponent.DockerRegistry regCred = componentCred.getDockerReg();
    final String usernameNoCred = regNoCred.userName;
    final String passwordNoCred = regNoCred.password;
    final String usernameCred = regCred.userName;
    final String passwordCred = regCred.password;

    assertEquals(usernameNoCred, "");
    assertEquals(passwordNoCred, "");
    assertEquals(usernameCred, username);
    assertEquals(passwordCred, password);
  }
}
