package com.example.myapplication.models;

import java.io.Serializable;

public class CartItem implements Serializable {

    private String id;
    private String name;
    private String imageUrl;
    private double price;
    private int quantity;
    private String brand;
    private String bottleSize;

    // Constructeur vide nécessaire pour Firebase / serialization
    public CartItem() {
    }

    // Constructeur pratique pour créer rapidement un objet
    public CartItem(String id, String name, String imageUrl, double price, int quantity, String brand, String bottleSize) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.brand = brand;
        this.bottleSize = bottleSize;
    }

    // Getters et setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getBottleSize() { return bottleSize; }
    public void setBottleSize(String bottleSize) { this.bottleSize = bottleSize; }
}
