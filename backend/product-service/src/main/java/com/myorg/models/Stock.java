package com.myorg.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Stock {
  private String productId;
  private int count;

  public Stock() {
  }

  public Stock(String productId, int count) {
    this.productId = productId;
    this.count = count;
  }

  @DynamoDbPartitionKey
  @DynamoDbAttribute("product_id")
  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  @DynamoDbAttribute("count")
  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}