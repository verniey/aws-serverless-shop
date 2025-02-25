package com.myorg.models;

public class Product {
  private String id;
  private String title;
  private String description;
  private double price;

  // Default constructor (required for Gson)
  public Product() {}

  public Product(String id, String name, String description, double price) {
    this.id = id;
    this.title = name;
    this.description = description;
    this.price = price;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}
