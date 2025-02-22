package com.myorg.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.myorg.services.ProductService;
import com.google.gson.Gson;
import java.util.Map;

public class GetProductsListHandler implements RequestHandler<Map<String, Object>, String> { // 🔥 Changed Object to Map<String, Object>
  private static final Gson gson = new Gson();
  private final ProductService productService = new ProductService();

  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    return gson.toJson(productService.getAllProducts());
  }
}
