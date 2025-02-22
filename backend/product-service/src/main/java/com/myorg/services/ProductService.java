package com.myorg.services;

import com.myorg.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

public class ProductService {
  private List<Product> products;

  public ProductService() {
    // Load products from the JSON file
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("products.json")) {
      if (inputStream == null) { // 🔥 Check if the file exists
        throw new RuntimeException("Error: products.json not found");
      }
      try (Reader reader = new InputStreamReader(inputStream)) {
        Gson gson = new Gson();
        Type productListType = new TypeToken<List<Product>>() {}.getType();
        products = gson.fromJson(reader, productListType);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load products from JSON file", e);
    }
  }

  public List<Product> getAllProducts() {
    return products;
  }

  public Product getProductById(String productId) {
    return products.stream()
        .filter(p -> p.getId().equals(productId))
        .findFirst()
        .orElse(null);
  }
}
