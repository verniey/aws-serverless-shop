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

import java.util.*;

public class GetProductsListHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final DynamoDbClient dynamoDbClient;
  private final String productsTable = "products";
  private final String stocksTable = "stocks";

  public GetProductsListHandler() {
    this.dynamoDbClient = DynamoDbClient.create();  // Initialize the DynamoDB client
  }

  public GetProductsListHandler(DynamoDbClient mockDynamoDbClient) {
    this.dynamoDbClient = DynamoDbClient.create();
  }


  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

    // Additional (optional) tasks - Log incoming request details
    LoggingUtils.logIncomingRequest(event, context);

    try {
      context.getLogger().log("Fetching products...\n");

      // Scan the products table
      context.getLogger().log("Scanning products table: " + productsTable + "\n");
      ScanResponse productsResponse = dynamoDbClient.scan(ScanRequest.builder()
          .tableName(productsTable)
          .build());
      context.getLogger().log("Products scan response: " + productsResponse.items() + "\n");

      // Scan the stocks table
      context.getLogger().log("Scanning stocks table: " + stocksTable + "\n");
      ScanResponse stocksResponse = dynamoDbClient.scan(ScanRequest.builder()
          .tableName(stocksTable)
          .build());
      context.getLogger().log("Stocks scan response: " + stocksResponse.items() + "\n");

      // Create a map of product_id -> count from stocks
      Map<String, Integer> stockMap = new HashMap<>();
      for (Map<String, AttributeValue> item : stocksResponse.items()) {
        if (item.containsKey("product_id") && item.containsKey("count")) {
          try {
            String productId = item.get("product_id").s();
            int count = Integer.parseInt(item.get("count").n());
            stockMap.put(productId, count);
          } catch (Exception e) {
            context.getLogger().log("Error processing stock item: " + item + "\n");
          }
        } else {
          context.getLogger().log("Missing product_id or count in stock item: " + item + "\n");
        }
      }

      // Convert DynamoDB products into ProductDto with stock count
      List<ProductDto> products = new ArrayList<>();
      for (Map<String, AttributeValue> item : productsResponse.items()) {
        try {
          String id = item.get("id").s();
          String title = item.get("title").s();
          String description = item.get("description").s();
          int price = Integer.parseInt(item.get("price").n());
          int count = stockMap.getOrDefault(id, 0);

          products.add(new ProductDto(id, count, title, description, price));
        } catch (Exception e) {
          context.getLogger().log("Error processing product item: " + item + "\n");
        }
      }

      context.getLogger().log("Fetched products: " + products + "\n");
      return createSuccessResponse(200, products);
    } catch (Exception e) {
      context.getLogger().log("Error fetching products: " + e.getMessage() + "\n");
      e.printStackTrace();
      return createErrorResponse(500, "Error fetching products from DynamoDB: " + e.getMessage());
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