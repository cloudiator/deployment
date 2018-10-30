package org.example.cloudformation;

import io.github.cloudiator.deployment.faasagent.cloudformation.CloudformationTemplateGenerator;
import org.example.cloudformation.resources.ExampleModels;
import org.junit.Test;

public class CloudformationTemplateGeneratorTest {

  @Test
  public void getTemplate() {
    CloudformationTemplateGenerator generator = new CloudformationTemplateGenerator(ExampleModels.getAppModelTwo());
    System.out.println(generator.getUpdateTemplate());
  }
}