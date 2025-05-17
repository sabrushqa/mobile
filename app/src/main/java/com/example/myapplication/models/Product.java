package com.example.myapplication.models;

import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private int quantity;
    private String brand;
    private String bottleSize;
    private double price;
    private String category;

    public Product() {
        // Constructeur vide requis pour Firebase
    }

    public Product(String id, String name, String description, List<String> imageUrls,
                   int quantity, String brand, String bottleSize, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.quantity = quantity;
        this.brand = brand;
        this.bottleSize = bottleSize;
        this.price = price;
        this.category = category;
    }

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getBottleSize() { return bottleSize; }
    public void setBottleSize(String bottleSize) { this.bottleSize = bottleSize; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
