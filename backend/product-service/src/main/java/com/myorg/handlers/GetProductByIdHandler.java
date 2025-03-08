package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.dto.ProductDto;
import com.myorg.utils.LoggingUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

public class GetProductByIdHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String PRODUCTS_TABLE = "products";
  private static final String STOCKS_TABLE = "stocks";
  private final DynamoDbClient dynamoDbClient;

  public GetProductByIdHandler() {
    this.dynamoDbClient = DynamoDbClient.create();

  }
  public GetProductByIdHandler(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
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

      // Fetch product from DynamoDB
      GetItemResponse productResponse = dynamoDbClient.getItem(GetItemRequest.builder()
          .tableName(PRODUCTS_TABLE)
          .key(Map.of("id", AttributeValue.builder().s(productId).build()))
          .build());

      if (!productResponse.hasItem()) {
        return createErrorResponse(404, "Product not found");
      }

      // Fetch stock data
      GetItemResponse stockResponse = dynamoDbClient.getItem(GetItemRequest.builder()
          .tableName(STOCKS_TABLE)
          .key(Map.of("product_id", AttributeValue.builder().s(productId).build()))
          .build());

      int count = stockResponse.hasItem() ? Integer.parseInt(stockResponse.item().get("count").n()) : 0;

      // Map to DTO
      ProductDto productDto = new ProductDto(
          productResponse.item().get("id").s(),
          count,
          productResponse.item().get("title").s(),
          productResponse.item().get("description").s(),
          Integer.parseInt(productResponse.item().get("price").n())
      );

      return createSuccessResponse(200, productDto);
    } catch (Exception e) {
      context.getLogger().log("Error fetching product: " + e.getMessage());
      return createErrorResponse(500, "Error fetching product from DynamoDB: " + e.getMessage());
    }
  }

  private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object body) {
    try {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(statusCode)
          .withHeaders(Map.of(
              "Content-Type", "application/json",
              "Access-Control-Allow-Origin", "*"
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
