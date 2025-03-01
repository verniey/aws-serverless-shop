package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Disabled
class GetProductsListHandlerTest {
  private GetProductsListHandler handler;
  private Context mockContext;
  private DynamoDbClient mockDynamoDbClient;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockDynamoDbClient = mock(DynamoDbClient.class);
    handler = new GetProductsListHandler(mockDynamoDbClient);
    mockContext = mock(Context.class);

    LambdaLogger mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
  }

  @Test
  @DisplayName("✅ Should return product list successfully")
  void testProductsListReturned() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock DynamoDB scan response for products table
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

    // Mock DynamoDB scan response for stocks table
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

    // Mock DynamoDB scan calls
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenAnswer(invocation -> {
          ScanRequest request = invocation.getArgument(0);
          if ("products".equals(request.tableName())) {
            return mockProductsResponse;
          } else if ("stocks".equals(request.tableName())) {
            return mockStocksResponse;
          }
          return ScanResponse.builder().items(List.of()).build();
        });

    // Invoke the handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assert the response
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    List<ProductDto> actualProducts = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});
    assertThat(actualProducts).hasSize(2);
    assertThat(actualProducts.get(0).getId()).isEqualTo("1");
    assertThat(actualProducts.get(0).getCount()).isEqualTo(10); // Verify stock count
    assertThat(actualProducts.get(1).getId()).isEqualTo("2");
    assertThat(actualProducts.get(1).getCount()).isEqualTo(5); // Verify stock count
  }

  @Test
  @DisplayName("✅ Should return empty list when no products exist")
  void testEmptyProductsList() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock empty DynamoDB scan responses
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenReturn(ScanResponse.builder().items(List.of()).build());

    // Invoke the handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assert the response
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    List<ProductDto> actualProducts = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});
    assertThat(actualProducts).isEmpty();
  }

  @Test
  @DisplayName("✅ Should return 500 when scan operation fails")
  void testScanFailure() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Mock DynamoDB scan to throw an exception
    when(mockDynamoDbClient.scan(any(ScanRequest.class)))
        .thenThrow(new RuntimeException("DynamoDB scan failed"));

    // Invoke the handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assert the response
    assertThat(responseEvent.getStatusCode()).isEqualTo(500);
    assertThat(responseEvent.getBody()).contains("Error fetching products from DynamoDB");
  }
}
