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
  private static String containerPort;
  private static String taskNoCredName, taskCredName, taskNoImagePortName;
  private static String jobNoCredName, jobCredName, jobNoImagePortName;
  private static String dockerCreateOptionsString, dockerCreateOsCommandString, dockerCreateArgsString;
  private static String dockerCreateOptionsNoImagePortString;
  private static String dockerStartOptionsString;
  private static Map<String,String> envMapNoCred, envMapCred, envMapNoImagePort;
  private static Task taskNoCred, taskCred, taskNoImagePort;
  private static Job jobNoCred, jobCred, jobNoImagePort;
  private static RemoteDockerComponent componentNoCred, componentCred, componentNoImagePort;

  @BeforeClass
  public static void setup() {
    setupStringFields();

    //no credentials, no port
    DockerInterface dIfaceNoCred = new DockerInterfaceImpl(absoluteImageNameNoPort, envMapNoCred);
    //credentials and port
    DockerInterface dIfaceCred = new DockerInterfaceImpl(absoluteImageNamePort, envMapCred);
    //no credentials, no port, noImagePort
    DockerInterface dIfaceNoImagePort = new DockerInterfaceImpl(absoluteImageNameNoPort, envMapNoImagePort);

    taskNoCred = TaskBuilder.newBuilder().name(taskNoCredName).addPorts(new ArrayList<>())
        .addInterface(dIfaceNoCred).addRequirements(new ArrayList<>()).optimization(null).build();
    taskCred = TaskBuilder.newBuilder().name(taskCredName).addPorts(new ArrayList<>())
        .addInterface(dIfaceCred).addRequirements(new ArrayList<>()).optimization(null).build();
    taskNoImagePort = TaskBuilder.newBuilder().name(taskNoImagePortName).addPorts(new ArrayList<>())
        .addInterface(dIfaceNoImagePort).addRequirements(new ArrayList<>()).optimization(null).build();

    jobNoCred = JobBuilder.newBuilder().generateId().name(jobNoCredName)
        .addCommunications(new HashSet<>()).addTask(taskNoCred).build();
    jobCred = JobBuilder.newBuilder().generateId().name(jobCredName)
        .addCommunications(new HashSet<>()).addTask(taskCred).build();
    jobNoImagePort = JobBuilder.newBuilder().generateId().name(jobNoImagePortName)
        .addCommunications(new HashSet<>()).addTask(taskNoImagePort).build();


    componentNoCred = new PrivateDockerComponentSupplier(jobNoCred, taskNoCred).get();
    componentCred = new PrivateDockerComponentSupplier(jobCred, taskCred).get();
    componentNoImagePort = new PrivateDockerComponentSupplier(jobNoImagePort, taskNoImagePort).get();
  }

  private static void setupStringFields() {
    taskNoCredName = "taskNoCred";
    taskCredName = "taskCred";
    taskNoImagePortName = "taskNoImagePort";
    jobNoCredName = "jobNoCred";
    jobCredName = "jobCred";
    jobNoImagePortName = "jobNoImagePort";
    hostName = "my-local-reg.de";
    port = 443;
    imageNameSpace = "namespace";
    imageName = "dockerImage";
    tag = "latest";
    absoluteImageNameNoPort = hostName + "/" + imageNameSpace + "/" + imageName + ":" + tag;
    absoluteImageNamePort = hostName + ":" + Integer.toString(port) + "/" + imageNameSpace + "/" + imageName + ":" + tag;
    username = "john-doe";
    password = "top-secret";
    containerPort = "8000:8001";
    envMapNoCred = new HashMap<>();
    envMapNoCred.put("foo","bar");
    envMapNoCred.put("john","doe");
    envMapNoCred.put("port",containerPort);
    envMapCred = new HashMap<>();
    envMapCred.putAll(envMapNoCred);
    envMapCred.put("username", username);
    envMapCred.put("password", password);
    envMapNoImagePort = new HashMap<>();
    envMapNoImagePort.put("foo","bar");
    dockerCreateOptionsString = "--interactive  --env foo=bar --env john=doe --restart no --publish 8000:8001";
    dockerCreateOptionsNoImagePortString = "--interactive  --env foo=bar --restart no";
    dockerCreateOsCommandString = "bash";
    dockerCreateArgsString = "--noediting";
    dockerStartOptionsString = "--interactive";
  }

  @Test
  public void testDockerCreateOptions() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();
    EntireDockerCommands cmdsNoIPort = componentNoImagePort.getEntireDockerCommands();

    testDockerOptions(cmdsNoCred, Type.CREATE, dockerCreateOptionsString);
    testDockerOptions(cmdsCred, Type.CREATE, dockerCreateOptionsString);
    testDockerOptions(cmdsNoIPort, Type.CREATE, dockerCreateOptionsNoImagePortString);
  }

  @Test
  public void testDockerStartOptions() {
    EntireDockerCommands cmdsNoCred = componentNoCred.getEntireDockerCommands();
    EntireDockerCommands cmdsCred = componentCred.getEntireDockerCommands();
    EntireDockerCommands cmdsNoIPort = componentNoImagePort.getEntireDockerCommands();

    testDockerOptions(cmdsNoCred, Type.START, dockerStartOptionsString);
    testDockerOptions(cmdsCred, Type.START, dockerStartOptionsString);
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
    EntireDockerCommands cmdsNoIPort = componentNoImagePort.getEntireDockerCommands();

    testDockerOsCommand(cmdsNoCred, Type.CREATE, dockerCreateOsCommandString);
    testDockerOsCommand(cmdsCred, Type.CREATE, dockerCreateOsCommandString);
    testDockerOsCommand(cmdsNoIPort, Type.CREATE, dockerCreateOsCommandString);
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
    EntireDockerCommands cmdsNoIPort = componentNoImagePort.getEntireDockerCommands();

    testDockerArgs(cmdsNoCred, Type.CREATE, dockerCreateArgsString);
    testDockerArgs(cmdsCred, Type.CREATE, dockerCreateArgsString);
    testDockerArgs(cmdsNoIPort, Type.CREATE, dockerCreateArgsString);
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
    final String noIPortNameSpace = componentNoImagePort.getImageFolder().trim();

    assertEquals(noCredNameSpace, imageNameSpace);
    assertEquals(credNameSpace, imageNameSpace);
    assertEquals(noIPortNameSpace, imageNameSpace);
  }

  @Test
  public void testImageName() {
    final String noCredImageName = componentNoCred.getImageName().trim();
    final String credImageName = componentCred.getImageName().trim();
    final String noIPortImageName = componentNoImagePort.getImageName().trim();

    assertEquals(noCredImageName, imageName);
    assertEquals(credImageName, imageName);
    assertEquals(noIPortImageName, imageName);
  }

  @Test
  public void testTag() {
    final String noCredTag = componentNoCred.getTag().trim();
    final String credTag = componentCred.getTag().trim();
    final String noIPortTag = componentNoImagePort.getTag().trim();

    assertEquals(noCredTag, tag);
    assertEquals(credTag, tag);
    assertEquals(noIPortTag, tag);
  }

  @Test
  public void testFullImageName() {
    final String fullImageNameNoCred = componentNoCred.getFullImageName();
    final String fullImageNameCred = componentCred.getFullImageName();
    final String fullImageNameNoIport = componentNoImagePort.getFullImageName();

    //no credentials, no port
    assertEquals(fullImageNameNoCred, absoluteImageNameNoPort);
    //credentials and port
    assertEquals(fullImageNameCred, absoluteImageNamePort);
    //no credentials, no port, no Image Port
    assertEquals(fullImageNameNoIport, absoluteImageNameNoPort);
  }

  @Test
  public void testHostName() {
    final String uriEndPointNoCred = componentNoCred.getUriEndPoint();
    final String uriEndPointCred = componentCred.getUriEndPoint();
    final String uriEndPointNoIPort = componentNoImagePort.getUriEndPoint();

    final String hostNameNoCred = uriEndPointNoCred.split(":")[0];
    final String hostNameCred = uriEndPointCred.split(":")[0];
    final String hostNameNoIPort = uriEndPointNoIPort.split(":")[0];

    assertEquals(hostNameNoCred, hostName);
    assertEquals(hostNameCred, hostName);
    assertEquals(hostNameNoIPort, hostName);
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
    final int getPortNoIPort = componentNoImagePort.getDockerReg().port;
    final String uriEndPointNoIPort = componentNoImagePort.getUriEndPoint();
    //no port
    final List<String> uriSplitNoIPort = new ArrayList<>(Arrays.asList(uriEndPointNoIPort.split(":")));


    //no port
    assertEquals(getPortNoCred,-1);
    assertEquals(getPortNoIPort,-1);
    //port
    assertEquals(getPortCred,port);
    //no port
    assertEquals(uriSplitNoCred.size(), 1);
    assertEquals(uriSplitNoIPort.size(), 1);
    //port
    assertEquals(uriSplitCred.size(), 2);
    //port
    assertEquals(Integer.parseInt(uriSplitCred.get(1)), port);
  }

  @Test
  public void testCredentials() {
    final RemoteDockerComponent.DockerRegistry regNoCred = componentNoCred.getDockerReg();
    final RemoteDockerComponent.DockerRegistry regCred = componentCred.getDockerReg();
    final RemoteDockerComponent.DockerRegistry regNoIport = componentNoImagePort.getDockerReg();
    final String usernameNoCred = regNoCred.userName;
    final String passwordNoCred = regNoCred.password;
    final String usernameCred = regCred.userName;
    final String passwordCred = regCred.password;
    final String usernameNoIPort = regNoIport.userName;
    final String passwordNoIPort = regNoIport.password;

    assertEquals(usernameNoCred, "");
    assertEquals(passwordNoCred, "");
    assertEquals(usernameCred, username);
    assertEquals(passwordCred, password);
    assertEquals(usernameNoIPort, "");
    assertEquals(passwordNoIPort, "");
  }
}
