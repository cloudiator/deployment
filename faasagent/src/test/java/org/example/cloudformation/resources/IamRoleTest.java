package org.example.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.resources.IamRole;
import org.junit.Test;

public class IamRoleTest {

  @Test
  public void testGetProperties() {
    IamRole role = new IamRole(ExampleModels.getAppModelTwo());
    System.out.println(role.asJson().toString(2));
  }
}