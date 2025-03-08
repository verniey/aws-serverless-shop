package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.utils.LoggingUtils;
import com.myorg.validators.ProductValidator;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class CreateProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String PRODUCTS_TABLE = "products";
  private static final String STOCKS_TABLE = "stocks";
  private final DynamoDbClient dynamoDbClient;

  public CreateProductHandler() {
    this.dynamoDbClient = DynamoDbClient.create();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

    // Additional (optional) tasks - Log incoming request details
    LoggingUtils.logIncomingRequest(event, context);

    try {
      JsonNode jsonBody = objectMapper.readTree(event.getBody());

      // Additional (optional) tasks - Validate required fields
      ProductValidator.validateProductData(jsonBody);

      // Generate product ID
      String productId = UUID.randomUUID().toString();

      // Extract values
      String title = jsonBody.get("title").asText();
      String description = jsonBody.has("description") ? jsonBody.get("description").asText() : "";
      int price = jsonBody.get("price").asInt();
      int count = jsonBody.has("count") ? jsonBody.get("count").asInt() : 0;

      // Additional (optional) tasks - Create transaction request
      TransactWriteItemsRequest transactionRequest = TransactWriteItemsRequest.builder()
          .transactItems(Arrays.asList(
              // Insert into products table
              TransactWriteItem.builder()
                  .put(Put.builder()
                      .tableName(PRODUCTS_TABLE)
                      .item(Map.of(
                          "id", AttributeValue.builder().s(productId).build(),
                          "title", AttributeValue.builder().s(title).build(),
                          "description", AttributeValue.builder().s(description).build(),
                          "price", AttributeValue.builder().n(String.valueOf(price)).build()
                      ))
                      .build())
                  .build(),

              // Insert into stocks table
              TransactWriteItem.builder()
                  .put(Put.builder()
                      .tableName(STOCKS_TABLE)
                      .item(Map.of(
                          "product_id", AttributeValue.builder().s(productId).build(),
                          "count", AttributeValue.builder().n(String.valueOf(count)).build()
                      ))
                      .build())
                  .build()
          ))
          .build();

      // Execute transaction
      dynamoDbClient.transactWriteItems(transactionRequest);

      // Create response object
      Map<String, Object> responseBody = Map.of(
          "id", productId,
          "count", count,
          "title", title,
          "description", description,
          "price", price
      );

      return createSuccessResponse(201, responseBody);

    } catch (IllegalArgumentException e) {
      // Handle validation errors
      return createErrorResponse(400, e.getMessage());
    } catch (Exception e) {
      // Handle other errors
      context.getLogger().log("Error creating product: " + e.getMessage());
      return createErrorResponse(500, "Error creating product: " + e.getMessage());
    }
  }

  private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object body) {
    try {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(statusCode)
          .withHeaders(Map.of("Content-Type", "application/json", "Access-Control-Allow-Origin", "*"))
          .withBody(objectMapper.writeValueAsString(body));
    } catch (Exception e) {
      return createErrorResponse(500, "Error processing response");
    }
  }

  private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(statusCode)
        .withHeaders(Map.of("Content-Type", "application/json", "Access-Control-Allow-Origin", "*"))
        .withBody("{\"error\": \"" + message + "\"}");
  }
}
