package com.myorg.validators;

import com.fasterxml.jackson.databind.JsonNode;

public class ProductValidator {

  /**
   * Validates the product data.
   *
   * @param jsonBody The JSON body containing product data.
   * @throws IllegalArgumentException If the product data is invalid.
   */
  public static void validateProductData(JsonNode jsonBody) throws IllegalArgumentException {
    // Validate title
    if (!jsonBody.has("title") || jsonBody.get("title").asText().isEmpty()) {
      throw new IllegalArgumentException("Field 'title' is required and cannot be empty");
    }

    // Validate price
    if (!jsonBody.has("price") || jsonBody.get("price").asInt() <= 0) {
      throw new IllegalArgumentException("Field 'price' is required and must be a positive number");
    }

    // Validate count (if provided)
    if (jsonBody.has("count") && jsonBody.get("count").asInt() < 0) {
      throw new IllegalArgumentException("Field 'count' must be a non-negative number");
    }
  }
}