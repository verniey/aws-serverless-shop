package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class GetProductsListHandlerTest {
  private GetProductsListHandler handler;
  private Context mockContext;
  private DynamoDbClient mockDynamoDbClient;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    // Mock DynamoDB client
    mockDynamoDbClient = mock(DynamoDbClient.class);

    // Initialize the handler with the mocked DynamoDB client
    handler = new GetProductsListHandler(mockDynamoDbClient);

    // Mock Lambda context and logger
    mockContext = mock(Context.class);
    LambdaLogger mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
  }

  @Test
  @DisplayName("✅ Should return product list successfully")
  void testProductsListReturned() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock response for products table
    ScanResponse mockProductsResponse = ScanResponse.builder()
        .items(List.of(
            Map.of(
                "id", AttributeValue.builder().s("1").build(),
                "title", AttributeValue.builder().s("Product A").build(),
                "description", AttributeValue.builder().s("Description A").build(),
                "price", AttributeValue.builder().n("50").build()
            ),
            Map.of(
                "id", AttributeValue.builder().s("2").build(),
                "title", AttributeValue.builder().s("Product B").build(),
                "description", AttributeValue.builder().s("Description B").build(),
                "price", AttributeValue.builder().n("75").build()
            )
        ))
        .build();

    // Mock response for stocks table
    ScanResponse mockStocksResponse = ScanResponse.builder()
        .items(List.of(
            Map.of(
                "product_id", AttributeValue.builder().s("1").build(),
                "count", AttributeValue.builder().n("10").build()
            ),
            Map.of(
                "product_id", AttributeValue.builder().s("2").build(),
                "count", AttributeValue.builder().n("5").build()
            )
        ))
        .build();

    // Ensure only expected products are returned
    when(mockDynamoDbClient.scan(argThat(req -> "products".equals(req.tableName()))))
        .thenReturn(mockProductsResponse);
    when(mockDynamoDbClient.scan(argThat(req -> "stocks".equals(req.tableName()))))
        .thenReturn(mockStocksResponse);

    // Invoke handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    List<ProductDto> actualProducts = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {
    });
    assertThat(actualProducts).hasSize(2); // Ensure only expected products are returned
  }

  @Test
  @DisplayName("✅ Should return empty list when no products exist")
  void testEmptyProductsList() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock empty responses
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(List.of()).build());

    // Invoke handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    List<ProductDto> actualProducts = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {
    });
    assertThat(actualProducts).isEmpty(); // Expecting empty list
  }

  @Test
  @DisplayName("❌ Should return 500 when scan operation fails")
  void testScanFailure() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock exception in scan operation
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenThrow(new RuntimeException("DynamoDB scan failed"));

    // Invoke handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assertions
    assertThat(responseEvent.getStatusCode()).isEqualTo(500);
    assertThat(responseEvent.getBody()).contains("Error fetching products from DynamoDB");
  }
}