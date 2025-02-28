package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.mock;

class GetProductsListHandlerTest {
  private GetProductsListHandler handler;
  private Context mockContext;
  private ObjectMapper objectMapper;
  private List<Product> expectedProducts;

  @BeforeEach
  void setUp() throws IOException {
    handler = new GetProductsListHandler();
    mockContext = mock(Context.class);
    objectMapper = new ObjectMapper();

    expectedProducts = loadProductsFromJson("products.json");
  }


  @Test
  void testProductsListReturned() throws Exception {
    // Simulate API Gateway request
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Call Lambda handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assert status code
    assertThat(responseEvent.getStatusCode()).isEqualTo(200);

    // Parse response body
    List<Product> actualProducts = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});

    // Ensure lists have the same size
    assertThat(actualProducts).hasSameSizeAs(expectedProducts);

    // Assert product list content
    assertThat(actualProducts)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyElementsOf(expectedProducts);
  }

  private List<Product> loadProductsFromJson(String resourcePath) throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
    if (inputStream == null) {
      throw new IOException("Test resource file not found: " + resourcePath);
    }
    return objectMapper.readValue(inputStream, new TypeReference<>() {});
  }


}
