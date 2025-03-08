package com.myorg.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.dto.ProductDto;
import com.myorg.handlers.GetProductByIdHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductByIdHandlerTest {

  private GetProductByIdHandler handler;
  private Context mockContext;
  private LambdaLogger mockLogger;
  private DynamoDbClient mockDynamoDbClient;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockDynamoDbClient = mock(DynamoDbClient.class);
    mockContext = mock(Context.class);
    mockLogger = mock(LambdaLogger.class);

    // Ensure context.getLogger() returns a mock logger
    when(mockContext.getLogger()).thenReturn(mockLogger);

    handler = new GetProductByIdHandler(mockDynamoDbClient);
  }

  @Test
  void testMissingProductId() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(new HashMap<>()); // No productId

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(400, responseEvent.getStatusCode());
    assertEquals("{\"error\": \"Invalid productId\"}", responseEvent.getBody());

    verify(mockLogger, atLeastOnce()).log(anyString());
  }

  @Test
  void testProductNotFound() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "non-existing-id"));

    when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(GetItemResponse.builder().build()); // Simulate empty response

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(404, responseEvent.getStatusCode());
    assertEquals("{\"error\": \"Product not found\"}", responseEvent.getBody());

    verify(mockLogger, atLeastOnce()).log(anyString());
  }

  @Test
  void testProductFound() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "valid-id"));

    // Mock DynamoDB product response
    GetItemResponse productResponse = GetItemResponse.builder()
        .item(Map.of(
            "id", AttributeValue.builder().s("valid-id").build(),
            "title", AttributeValue.builder().s("Test Product").build(),
            "description", AttributeValue.builder().s("A test product description").build(),
            "price", AttributeValue.builder().n("150").build()
        ))
        .build();

    // Mock DynamoDB stock response
    GetItemResponse stockResponse = GetItemResponse.builder()
        .item(Map.of(
            "product_id", AttributeValue.builder().s("valid-id").build(),
            "count", AttributeValue.builder().n("10").build()
        ))
        .build();

    when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(productResponse) // First call - product table
        .thenReturn(stockResponse);  // Second call - stocks table

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(200, responseEvent.getStatusCode());

    ProductDto responseProduct = objectMapper.readValue(responseEvent.getBody(), ProductDto.class);
    assertEquals("valid-id", responseProduct.getId());
    assertEquals("Test Product", responseProduct.getTitle());
    assertEquals("A test product description", responseProduct.getDescription());
    assertEquals(150, responseProduct.getPrice());
    assertEquals(10, responseProduct.getCount());

    verify(mockLogger, atLeastOnce()).log(anyString());
  }

  @Test
  void testDatabaseError() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "valid-id"));

    when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenThrow(new RuntimeException("Database connection failed"));

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(500, responseEvent.getStatusCode());
    assertTrue(responseEvent.getBody().contains("Error fetching product from DynamoDB"));

    verify(mockLogger, atLeastOnce()).log(anyString());
  }
}
