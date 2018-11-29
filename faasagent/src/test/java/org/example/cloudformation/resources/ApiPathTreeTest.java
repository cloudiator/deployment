package org.example.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.resources.AbstractResource;
import io.github.cloudiator.deployment.faasagent.cloudformation.resources.ApiPathTree;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApiPathTreeTest {

  private List<LambdaTemplate> lambdas = new LinkedList<>();

  @Before
  public void setUp() {
    addLambdaWithPath("path/to/first");
    addLambdaWithPath("path/to/second");
    addLambdaWithPath("path/short");
    addLambdaWithPath("another/path");
  }

  private void addLambdaWithPath(String path) {
    LambdaTemplate lambda = new LambdaTemplate();
    lambda.httpPath = path;
    lambdas.add(lambda);
  }

  @Test
  public void testResourceGeneration() {
    List<AbstractResource> resources =
        new ApiPathTree(lambdas).getResources();

    for (AbstractResource resource : resources) {
      System.out.println(resource.asJson());
    }

    assertThat(resources.size(), is(equalTo(7)));
  }
}