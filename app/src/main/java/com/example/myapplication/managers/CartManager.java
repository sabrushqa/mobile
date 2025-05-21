package com.example.myapplication.managers;

import com.example.myapplication.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(CartItem item) {
        for (CartItem i : cartItems) {
            if (i.getId().equals(item.getId())) {
                i.setQuantity(i.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartItems.add(item);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
