package io.github.cloudiator.deployment.faasagent.cloudformation.resources;

import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.utils.TemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import com.sun.xml.internal.ws.util.StringUtils;

public class ApiPathTree {
  private List<String> paths;
  private Node resourceTree;
  public ApiPathTree(List<LambdaTemplate> lambdas) {
    this.paths = lambdas.stream()
        .map((lambda) -> lambda.httpPath)
        .collect(Collectors.toList());
  }

  public List<AbstractResource> getResources() {
    buildTree();
    return getResources(resourceTree);
  }

  private List<AbstractResource> getResources(Node node) {
    List<AbstractResource> resources = new LinkedList<>();

    for (Node child : node.childrens.values()) {
      resources.add(new ApiGatewayResource(node.id, child.pathPart, child.name));
      resources.addAll(getResources(child));
    }

    return resources;
  }

  private void buildTree() {
    resourceTree = new Node();
    resourceTree.name = "root";
    resourceTree.id = TemplateUtils.getAtt(TemplateUtils.API_GATEWAY, "RootResourceId");

    for (String path : paths) {
      String[] pathParts = path.split("/");
      int idx = 0;
      Node parent = resourceTree;
      StringBuilder nameBuilder = new StringBuilder();
      for (String part : pathParts) {
        Node current = parent.childrens.computeIfAbsent(part, (key) -> new Node());
        nameBuilder.append(StringUtils.capitalize(part));
        if (current.name == null) {
          current.name = "ApiGatewayResource" + nameBuilder.toString();
          current.id = TemplateUtils.ref(current.name);
          current.pathPart = part;
        }
        parent = current;
        idx++;
      }
    }
  }

  static class Node {
    public String name;
    public JSONObject id;
    public String pathPart;
    public Map<String, Node> childrens = new HashMap<>();
  }
}
