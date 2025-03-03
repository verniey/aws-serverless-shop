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
    if (!jsonBody.has("title") || jsonBody.get("title").asText().trim().isEmpty()) {
      throw new IllegalArgumentException("Field 'title' is required and cannot be empty");
    }

    // Validate description
    if (!jsonBody.has("description") || jsonBody.get("description").asText().trim().isEmpty()) {
      throw new IllegalArgumentException("Field 'description' is required and cannot be empty");
    }

    // Validate price
    if (!jsonBody.has("price") || !jsonBody.get("price").isNumber() || jsonBody.get("price").asDouble() <= 0) {
      throw new IllegalArgumentException("Field 'price' must be a positive number.");
    }

    // Validate count
    if (!jsonBody.has("count") || !jsonBody.get("count").isInt() || jsonBody.get("count").asInt() < 0) {
      throw new IllegalArgumentException("Field 'count' must be a whole number (integer) and cannot be negative.");
    }

  }
}
