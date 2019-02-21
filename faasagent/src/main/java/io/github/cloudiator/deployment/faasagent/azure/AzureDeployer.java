package io.github.cloudiator.deployment.faasagent.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.ApplicationTemplate;
import io.github.cloudiator.deployment.faasagent.cloudformation.models.LambdaTemplate;
import io.github.cloudiator.deployment.faasagent.deployment.FaasDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class AzureDeployer implements FaasDeployer {

  public static class AzureDeployerFactory implements FaasDeployerFactory {

    @Override
    public boolean supports(Cloud cloud) {
      return "azure".equals(cloud.api().providerName());
    }

    @Override
    public FaasDeployer create(String region, Cloud cloud) {
      String clientTenant = cloud.credential().user();
      int colonIndex = clientTenant.lastIndexOf(':');
      String clientId = clientTenant.substring(0, colonIndex);
      String tenantId = clientTenant.substring(colonIndex + 1);
      return new AzureDeployer(region, clientId, tenantId, cloud.credential().password());
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureDeployer.class);

  private final Region region;
  private final Azure azure;

  private AzureDeployer(String region, String clientId, String tenantId, String password) {
    this.region = Region.fromName(region);
    AzureTokenCredentials credentials = new ApplicationTokenCredentials(
        clientId,
        tenantId,
        password,
        AzureEnvironment.AZURE
    );
    try {
      this.azure = Azure.configure()
          .withLogLevel(LogLevel.NONE)
          .authenticate(credentials)
          .withDefaultSubscription();
    } catch (IOException e) {
      LOGGER.error("Failed to create Azure API", e);
      throw new IllegalStateException(e);
    }
  }


  @Override
  public String deployApp(ApplicationTemplate app) {
    try {
      LOGGER.info("Creating function app: {}", app.name);

      Map<String, String> appEnvironment = app.functions.stream().map(fun -> fun.env)
          .collect(HashMap::new, Map::putAll, Map::putAll);

      FunctionApp functionApp = azure.appServices().functionApps()
          .define(app.name)
          .withRegion(region)
          .withNewResourceGroup()
          .withAppSettings(appEnvironment)
          .create();

      for (LambdaTemplate lambda : app.functions) {
        LOGGER.info("Deploying function {}", lambda.name);
        functionApp.deploy().withPackageUri(lambda.codeUrl).execute();
      }
      LOGGER.info("Function App depplyment finished {}", app.name);
      return functionApp.id();
    } catch (Exception e) {
      LOGGER.error("Failed to deploy Azure function app", e);
      removeApp(app.name);
      throw new IllegalStateException("Could not create function app", e);
    }
  }

  @Override
  public Map<String, String> getApiEndpoints(ApplicationTemplate app) {
    String pattern = "https://{0}.azurewebsites.net/api/{1}";
    Map<String, String> endpoints = new HashMap<>();
    for (LambdaTemplate lambda : app.functions) {
      String lambdaEndpoint = MessageFormat.format(pattern, app.name, lambda.httpPath);
      endpoints.put(lambda.name, lambdaEndpoint);
    }
    return endpoints;
  }

  @Override
  public void removeApp(String resourceGroup) {
    azure.resourceGroups().beginDeleteByName(resourceGroup);
  }
}
