package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;
import com.myorg.utils.LoggingUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.Map;

public class GetProductByIdHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String PRODUCTS_TABLE = "products"; // Hardcoded table name
  private final DynamoDbClient dynamoDbClient;

  // Constructor for dependency injection (used in tests)
  public GetProductByIdHandler(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  // Default constructor for production usage
  public GetProductByIdHandler() {
    this.dynamoDbClient = DynamoDbClient.create();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

    // Additional (optional) tasks - Log incoming request details
    LoggingUtils.logIncomingRequest(event, context);

    String productId = event.getPathParameters() != null ? event.getPathParameters().get("productId") : null;

    // Validate productId
    if (productId == null || productId.trim().isEmpty()) {
      return createErrorResponse(400, "Invalid productId");
    }

    try {
      context.getLogger().log("Fetching product with ID: " + productId);

      // Use the hardcoded table name
      GetItemResponse productResponse = dynamoDbClient.getItem(GetItemRequest.builder()
          .tableName(PRODUCTS_TABLE)
          .key(Map.of("id", AttributeValue.builder().s(productId).build()))
          .build());

      if (!productResponse.hasItem()) {
        context.getLogger().log("Product not found" + productId);
        return createErrorResponse(404, "Product not found");
      }

      Product product = mapToProduct(productResponse.item());
      context.getLogger().log("Fetched product: " + product);

      return createSuccessResponse(200, product);
    } catch (Exception e) {
      context.getLogger().log("Error fetching product: " + e.getMessage());
      return createErrorResponse(500, "Error fetching product from DynamoDB: " + e.getMessage());
    }
  }

  private Product mapToProduct(Map<String, AttributeValue> item) {
    return new Product(
        item.get("id").s(),
        item.get("title").s(),
        item.get("description").s(),
        Integer.parseInt(item.get("price").n())
    );
  }

  private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object body) {
    try {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(statusCode)
          .withHeaders(Map.of(
              "Content-Type", "application/json",
              "Access-Control-Allow-Origin", "*",
              "Access-Control-Allow-Methods", "GET,OPTIONS",
              "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"
          ))
          .withBody(objectMapper.writeValueAsString(body));
    } catch (Exception e) {
      return createErrorResponse(500, "Error processing response");
    }
  }

  private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(statusCode)
        .withHeaders(getResponseHeaders())
        .withBody("{\"error\": \"" + message + "\"}");
  }

  private Map<String, String> getResponseHeaders() {
    return Map.of(
        "Content-Type", "application/json",
        "Access-Control-Allow-Origin", "*" // Restrict to specific origins in production
    );
  }
}