package io.github.cloudiator.deployment.lance;

import static org.junit.Assert.assertEquals;

import de.uniulm.omi.cloudiator.lance.application.component.DockerComponent;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommand;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommand.Type;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.DockerCommandException;
import de.uniulm.omi.cloudiator.lance.lifecycle.language.EntireDockerCommands;
import io.github.cloudiator.deployment.domain.DockerInterface;
import io.github.cloudiator.deployment.domain.DockerInterfaceImpl;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.JobBuilder;
import io.github.cloudiator.deployment.domain.JobNewImpl;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.domain.TaskBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class PublicDockerComponentSupplierTest {

  private static String imageNameSpace, imageNameRelative;
  private static String imageTag;
  private static String taskSimpleName, taskExtendedName;
  private static String jobSimpleName, jobExtendedName;
  private static String dockerCreateOptionsString, dockerCreateOsCommandString, dockerCreateArgsString;
  private static String dockerStartOptionsString;
  private static String containerPort;
  private static String containerVolume;
  private static Map<String,String> envMap;
  private static Task taskSimple, taskExtended;
  private static Job jobSimple, jobExtended;
  private static DockerComponent componentSimple, componentExtended;
  private static String uId = "test";

  @BeforeClass
  public static void setup() {
    setupStringFields();

    DockerInterface dIfaceSimple = new DockerInterfaceImpl(imageNameRelative + ":" + imageTag, envMap);
    DockerInterface dIfaceExtended = new DockerInterfaceImpl(imageNameSpace + "/" + imageNameRelative + ":" + imageTag, envMap);

    taskSimple = TaskBuilder.newBuilder().name(taskSimpleName).addPorts(new ArrayList<>())
        .addInterface(dIfaceSimple).addRequirements(new ArrayList<>()).optimization(null).build();
    taskExtended = TaskBuilder.newBuilder().name(taskExtendedName).addPorts(new ArrayList<>())
        .addInterface(dIfaceExtended).addRequirements(new ArrayList<>()).optimization(null).build();

    jobSimple = JobBuilder.newBuilder().generateId().userId(uId).name(jobSimpleName)
        .addCommunications(new HashSet<>()).addTask(taskSimple).build();
    jobExtended = JobBuilder.newBuilder().generateId().userId(uId).name(jobExtendedName)
        .addCommunications(new HashSet<>()).addTask(taskExtended).build();

    componentSimple = new PublicDockerComponentSupplier(jobSimple, taskSimple).get();
    componentExtended = new PublicDockerComponentSupplier(jobExtended, taskExtended).get();
  }

  private static void setupStringFields() {
    taskSimpleName = "taskSimple";
    taskExtendedName = "taskExtended";
    jobSimpleName = "jobSimple";
    jobExtendedName = "jobExtended";
    imageNameSpace = "root/subDir";
    imageNameRelative = "imagename";
    imageTag = "latest";
    containerPort = "8000:8001";
    containerVolume = "/var/lib/mysql";
    envMap = new HashMap<>();
    envMap.put("foo","bar");
    envMap.put("john","doe");
    envMap.put("port",containerPort);
    envMap.put("volume",containerVolume);
    dockerCreateOptionsString = "--publish 8000:8001 --volume /var/lib/mysql --interactive  --env foo=bar --env john=doe --restart no";
    dockerCreateOsCommandString = "bash";
    dockerCreateArgsString = "--noediting";
    dockerStartOptionsString = "--interactive";
  }

  @Test
  public void testDockerCreateOptions() {
    EntireDockerCommands cmdsSimple = componentSimple.getEntireDockerCommands();
    EntireDockerCommands cmdsExtended = componentExtended.getEntireDockerCommands();

    testDockerOptions(cmdsSimple, Type.CREATE, dockerCreateOptionsString);
    testDockerOptions(cmdsExtended, Type.CREATE, dockerCreateOptionsString);
  }

  @Test
  public void testDockerStartOptions() {
    EntireDockerCommands cmdsSimple = componentSimple.getEntireDockerCommands();
    EntireDockerCommands cmdsExtended = componentExtended.getEntireDockerCommands();

    testDockerOptions(cmdsSimple, Type.START, dockerStartOptionsString);
    testDockerOptions(cmdsExtended, Type.START, dockerStartOptionsString);
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
    EntireDockerCommands cmdsSimple = componentSimple.getEntireDockerCommands();
    EntireDockerCommands cmdsExtended = componentExtended.getEntireDockerCommands();

    testDockerOsCommand(cmdsSimple, Type.CREATE, dockerCreateOsCommandString);
    testDockerOsCommand(cmdsExtended, Type.CREATE, dockerCreateOsCommandString);
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
    EntireDockerCommands cmdsSimple = componentSimple.getEntireDockerCommands();
    EntireDockerCommands cmdsExtended = componentExtended.getEntireDockerCommands();

    testDockerArgs(cmdsSimple, Type.CREATE, dockerCreateArgsString);
    testDockerArgs(cmdsExtended, Type.CREATE, dockerCreateArgsString);
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
    final String simpleNameSpace = componentSimple.getImageFolder().trim();
    final String extendedNameSpace = componentExtended.getImageFolder().trim();

    assertEquals(simpleNameSpace, "");
    assertEquals(extendedNameSpace, imageNameSpace);
  }

  @Test
  public void testImageName() {
    final String simpleImageName = componentSimple.getImageName().trim();
    final String extendedImageName = componentExtended.getImageName().trim();

    assertEquals(simpleImageName, imageNameRelative);
    assertEquals(extendedImageName, imageNameRelative);
  }

  @Test
  public void testTag() {
    final String simpleTag = componentSimple.getTag().trim();
    final String extendedTag = componentExtended.getTag().trim();

    assertEquals(simpleTag, imageTag);
    assertEquals(extendedTag, imageTag);
  }

  @Test
  public void testFullImageName() {
    final String fullImageNameSimple = componentSimple.getFullImageName();
    final String fullImageNameExtended = componentExtended.getFullImageName();

    final String fullRelativeImageName = imageNameRelative + ":" + imageTag;
    final String fullAbsoluteImageName = imageNameSpace + "/" + fullRelativeImageName;

    assertEquals(fullImageNameSimple, fullRelativeImageName);
    assertEquals(fullImageNameExtended, fullAbsoluteImageName);
  }
}
