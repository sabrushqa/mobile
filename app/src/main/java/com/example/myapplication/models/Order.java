package com.example.myapplication.models;

import java.io.Serializable;
import java.util.Map;

public class Order implements Serializable {


    private String id;
    private String email;
    private long timestamp;
    private Map<String, Object> items;
    private String status;
    private String address;
    private String city;
    private String firstName;
    private String lastName;
    private String phone;
    private Double total;

    public Order() {}

    public Order(String id, String email, long timestamp, Map<String, Object> items,
                 String status, String address, String city,
                 String firstName, String lastName, String phone, Double total) {
        this.id = id;
        this.email = email;
        this.timestamp = timestamp;
        this.items = items;
        this.status = status;
        this.address = address;
        this.city = city;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.total = total;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getItems() { return items; }
    public void setItems(Map<String, Object> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getTotal() { return total; } // corrigé ici
    public void setTotal(Double total) { this.total = total; } // corrigé ici

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

}
