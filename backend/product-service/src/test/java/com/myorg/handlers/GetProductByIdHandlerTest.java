package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetProductByIdHandlerTest {
  private GetProductByIdHandler handler;
  private Context mockContext;
  private DynamoDbClient mockDynamoDbClient;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockDynamoDbClient = mock(DynamoDbClient.class);
    handler = new GetProductByIdHandler(mockDynamoDbClient);
    mockContext = mock(Context.class);

    // Mock `getLogger()` to avoid NullPointerException
    LambdaLogger mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
  }

  @Test
  @DisplayName("✅ Should return product successfully when found by ID")
  void testProductFound() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "1"));

    // Mock response from DynamoDB
    GetItemResponse mockResponse = GetItemResponse.builder()
        .item(Map.of(
            "id", AttributeValue.builder().s("1").build(),
            "title", AttributeValue.builder().s("Product A").build(),
            "description", AttributeValue.builder().s("Description A").build(),
            "price", AttributeValue.builder().n("50").build()
        ))
        .build();

    when(mockDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockResponse);

    // Call Lambda handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    Product product = objectMapper.readValue(responseEvent.getBody(), Product.class);
    assertThat(product.getId()).isEqualTo("1");
    assertThat(product.getTitle()).isEqualTo("Product A");
  }

  @Test
  @DisplayName("✅ Should return 404 when product is not found")
  void testProductNotFound() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "999"));

    // Mock empty response
    when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(GetItemResponse.builder().build());

    // Call Lambda handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(404);
    assertThat(responseEvent.getBody()).contains("Product not found");
  }

  @Test
  @DisplayName("✅ Should return 500 when DynamoDB operation fails")
  void testDynamoDbError() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "1"));

    // Mock DynamoDB error
    when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenThrow(new RuntimeException("DynamoDB error"));

    // Call Lambda handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(500);
    assertThat(responseEvent.getBody()).contains("Error fetching product from DynamoDB");
  }
}