package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductsListHandlerTest {
  private GetProductsListHandler handler;
  private Context mockContext;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    handler = new GetProductsListHandler();
    mockContext = mock(Context.class);
  }

  @Test
  void testProductsListReturned() throws Exception {
    // Simulate API Gateway request
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

    // Call Lambda handler
    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    // Assert status code
    assertEquals(200, responseEvent.getStatusCode());

    // Parse response body
    List<Product> products = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});
    assertFalse(products.isEmpty(), "Product list should not be empty");

    // Check first product details
    assertEquals("1", products.get(0).getId());
    assertEquals("Book One", products.get(0).getName());
    assertEquals("A thrilling mystery novel that keeps you on edge.", products.get(0).getDescription());
    assertEquals(29, products.get(0).getPrice());

    // Check fifth product details
    assertEquals("5", products.get(4).getId());
    assertEquals("Book Five", products.get(4).getName());
    assertEquals("A thought-provoking exploration of human psychology.", products.get(4).getDescription());
    assertEquals(40, products.get(4).getPrice());
  }
}
