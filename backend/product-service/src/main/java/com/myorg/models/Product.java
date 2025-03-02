package com.myorg.models;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Product {
  private String id;
  private String title;
  private String description;
  private int price;

  public Product() {}

  public Product(String id, String title, String description, int price) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.price = price;
  }

  @DynamoDbPartitionKey
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public int getPrice() { return price; }
  public void setPrice(int price) { this.price = price; }
}
