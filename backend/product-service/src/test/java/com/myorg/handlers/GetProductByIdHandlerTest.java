package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductByIdHandlerTest {
  private GetProductByIdHandler handler;
  private Context mockContext;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    handler = new GetProductByIdHandler();
    mockContext = mock(Context.class);
  }

  @Test
  @DisplayName("Test that the product is returned successfully when found by ID")
  void testProductFound() throws Exception {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "1"));

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(200, responseEvent.getStatusCode());

    Product product = objectMapper.readValue(responseEvent.getBody(), Product.class);
    assertEquals("1", product.getId());
  }

  @Test
  @DisplayName("Test that the handler returns 404 when product is not found")
  void testProductNotFound() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(Map.of("productId", "999"));

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(404, responseEvent.getStatusCode());

    Map<String, String> errorResponse;
    try {
      errorResponse = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertEquals("Product not found", errorResponse.get("error"));
  }

  @Test
  @DisplayName("Test that the handler returns 400 when productId is missing")
  void testMissingProductId() {
    APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
    requestEvent.setPathParameters(null); // No path parameters

    APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, mockContext);

    assertEquals(400, responseEvent.getStatusCode());

    Map<String, String> errorResponse;
    try {
      errorResponse = objectMapper.readValue(responseEvent.getBody(), new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertEquals("Missing productId in path parameters", errorResponse.get("error"));
  }
}
