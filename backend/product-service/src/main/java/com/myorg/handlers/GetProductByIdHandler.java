package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GetProductByIdHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static List<Product> products;

  static {
    try (InputStream inputStream = GetProductByIdHandler.class.getClassLoader().getResourceAsStream("products.json")) {
      if (inputStream == null) {
        throw new RuntimeException("products.json file not found in resources");
      }
      products = objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load products.json", e);
    }
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    String productId = event.getPathParameters() != null ? event.getPathParameters().get("productId") : null;

    if (productId == null) {
      return createErrorResponse(400, "Missing productId in path parameters");
    }

    Optional<Product> productOpt = products.stream()
        .filter(p -> String.valueOf(p.getId()).equals(productId))
        .findFirst();

    if (productOpt.isEmpty()) {
      return createErrorResponse(404, "Product not found");
    }

    return createSuccessResponse(200, productOpt.get());
  }

  private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object body) {
    try {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(statusCode)
          .withHeaders(Map.of(
              "Content-Type", "application/json",
              "Access-Control-Allow-Origin", "*",
              "Access-Control-Allow-Methods", "GET",
              "Access-Control-Allow-Headers", "Content-Type"
          ))
          .withBody(objectMapper.writeValueAsString(body));
    } catch (Exception e) {
      return createErrorResponse(500, "Error processing response");
    }
  }


  private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(statusCode)
        .withHeaders(Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
        ))
        .withBody("{\"error\": \"" + message + "\"}");
  }



}
