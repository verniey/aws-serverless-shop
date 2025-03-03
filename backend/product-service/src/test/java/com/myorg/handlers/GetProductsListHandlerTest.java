package com.myorg.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled
class GetProductsListHandlerTest {

  private GetProductsListHandler handler;
  private Context mockContext;
  private LambdaLogger mockLogger;
  private DynamoDbClient mockDynamoDbClient;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockDynamoDbClient = mock(DynamoDbClient.class);
    mockContext = mock(Context.class);
    mockLogger = mock(LambdaLogger.class);

    when(mockContext.getLogger()).thenReturn(mockLogger);

    handler = new GetProductsListHandler(mockDynamoDbClient);
  }

  @Test
  void testGetProductsListSuccess() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock product response
    List<Map<String, AttributeValue>> productItems = List.of(
        Map.of(
            "id", AttributeValue.builder().s("1").build(),
            "title", AttributeValue.builder().s("Test Product 1").build(),
            "description", AttributeValue.builder().s("Description 1").build(),
            "price", AttributeValue.builder().n("100").build()
        ),
        Map.of(
            "id", AttributeValue.builder().s("2").build(),
            "title", AttributeValue.builder().s("Test Product 2").build(),
            "description", AttributeValue.builder().s("Description 2").build(),
            "price", AttributeValue.builder().n("200").build()
        )
    );
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(productItems).build());

    // Mock stock response
    List<Map<String, AttributeValue>> stockItems = List.of(
        Map.of(
            "product_id", AttributeValue.builder().s("1").build(),
            "count", AttributeValue.builder().n("5").build()
        ),
        Map.of(
            "product_id", AttributeValue.builder().s("2").build(),
            "count", AttributeValue.builder().n("10").build()
        )
    );
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(stockItems).build());

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(200, responseEvent.getStatusCode());

    List<ProductDto> products = objectMapper.readValue(responseEvent.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, ProductDto.class));

    assertEquals(2, products.size());
    assertEquals("1", products.get(0).getId());
    assertEquals(5, products.get(0).getCount());
    assertEquals("Test Product 1", products.get(0).getTitle());
    assertEquals(100, products.get(0).getPrice());

    assertEquals("2", products.get(1).getId());
    assertEquals(10, products.get(1).getCount());
    assertEquals("Test Product 2", products.get(1).getTitle());
    assertEquals(200, products.get(1).getPrice());

    verify(mockLogger, atLeastOnce()).log(anyString());
  }

  @Test
  void testGetProductsListMissingStockData() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock product response with no stock data
    List<Map<String, AttributeValue>> productItems = List.of(
        Map.of(
            "id", AttributeValue.builder().s("1").build(),
            "title", AttributeValue.builder().s("Test Product 1").build(),
            "description", AttributeValue.builder().s("Description 1").build(),
            "price", AttributeValue.builder().n("100").build()
        )
    );
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(productItems).build());

    // Mock empty stock response
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(new ArrayList<>()).build());

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(200, responseEvent.getStatusCode());

    List<ProductDto> products = objectMapper.readValue(responseEvent.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, ProductDto.class));

    assertEquals(1, products.size());
    assertEquals("1", products.get(0).getId());
    assertEquals(0, products.get(0).getCount()); // No stock found, should be 0
    assertEquals("Test Product 1", products.get(0).getTitle());
    assertEquals(100, products.get(0).getPrice());

    verify(mockLogger, atLeastOnce()).log(anyString());
  }

  @Test
  void testDatabaseError() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Simulate database failure
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenThrow(new RuntimeException("DynamoDB failure"));

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(500, responseEvent.getStatusCode());
    assertTrue(responseEvent.getBody().contains("Error fetching products from DynamoDB"));

    verify(mockLogger, atLeastOnce()).log(anyString());
  }
}
