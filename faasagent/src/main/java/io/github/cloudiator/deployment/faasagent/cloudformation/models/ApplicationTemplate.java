package io.github.cloudiator.deployment.faasagent.cloudformation.models;

import com.amazonaws.regions.Regions;

import java.io.File;
import java.util.List;

import static io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils.ZIP_FILE;

public class ApplicationTemplate {
  public String name; //Task name
  public Regions region; // based on node
  public List<LambdaTemplate> functions; // From task interfaces

  public String getFunctionFullName(LambdaTemplate fun) {
    return String.format("%s-%s", name, fun.name);
  }
}
