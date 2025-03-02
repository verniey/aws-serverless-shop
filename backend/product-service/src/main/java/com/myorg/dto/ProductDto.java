package com.myorg.dto;

public class ProductDto {
  private String id;
  private String title;
  private String description;
  private int price;
  private int count;

  public ProductDto() {}

  public ProductDto(String id, int count, String title, String description, int price) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.price = price;
    this.count = count;
  }

  public String getId() { return id; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public int getPrice() { return price; }
  public int getCount() { return count; }
}
