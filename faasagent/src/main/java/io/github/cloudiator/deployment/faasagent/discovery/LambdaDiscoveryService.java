package io.github.cloudiator.deployment.faasagent.discovery;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.pricing.AWSPricing;
import com.amazonaws.services.pricing.AWSPricingClientBuilder;
import com.amazonaws.services.pricing.model.Filter;
import com.amazonaws.services.pricing.model.FilterType;
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;

public class LambdaDiscoveryService {

  private static final String REQUEST = "AWS-Lambda-Requests";
  private static final String DURATION = "AWS-Lambda-Duration";

  private final AWSPricing pricingApi;

  public LambdaDiscoveryService() {
    // pricing API is only available in US_EAST_1 and AP_SOUTH_1
    pricingApi = AWSPricingClientBuilder.standard()
        .withRegion(Regions.US_EAST_1).build();
  }

  public BigDecimal getLambdaRequestPrice() {
    return getLambdaRequestPrice(Regions.DEFAULT_REGION);
  }

  public BigDecimal getLambdaDurationPrice() {
    return getLambdaDurationPrice(Regions.DEFAULT_REGION);
  }

  public BigDecimal getLambdaRequestPrice(Regions region) {
    return getPrice(REQUEST, region.getDescription());
  }

  public BigDecimal getLambdaDurationPrice(Regions region) {
    return getPrice(DURATION, region.getDescription());
  }

  private BigDecimal getPrice(String attr, String loc) {
    JsonParser parser = new JsonParser();
    GetProductsResult result = pricingApi.getProducts(new GetProductsRequest()
        .withServiceCode("AWSLambda")
        .withFilters(
            createFilter("group", attr),
            createFilter("location", loc)));

    BigDecimal price = BigDecimal.valueOf(Double.MAX_VALUE);

    // Price list should be a singleton
    for (String priceResponse : result.getPriceList()) {
      JsonElement elem = parser.parse(priceResponse);

      if (elem.isJsonObject()) {
        JsonElement pricePerUnit = findInJsonTree(elem.getAsJsonObject(), "pricePerUnit");
        if (pricePerUnit != null && pricePerUnit.isJsonObject()) {
          price = pricePerUnit.getAsJsonObject().get("USD").getAsBigDecimal();
        }
      }
    }
    return price;
  }

  private Filter createFilter(String key, String value) {
    return new Filter().withType(FilterType.TERM_MATCH)
        .withField(key).withValue(value);
  }

  private JsonElement findInJsonTree(JsonObject obj, String value) {
    if (obj.has(value)) {
      return obj.get(value);
    }
    // Recursively search for value in every child object
    for (String key : obj.keySet()) {
      JsonElement child = obj.get(key);
      if (child.isJsonObject()) {
        JsonElement result = findInJsonTree(child.getAsJsonObject(), value);
        if (result != null) {
          // value successfully found in child object
          return result;
        }
      }
    }
    // value not found in obj
    return null;
  }

}
