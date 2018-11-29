package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;

public class S3Bucket extends AbstractResource {

  @Override
  public String getName() {
    return TemplateUtils.BUCKET;
  }

  @Override
  protected String getType() {
    return "AWS::S3::Bucket";
  }
}
