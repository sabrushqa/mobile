package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.models.CartItem;
import com.example.myapplication.managers.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.*;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangedListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(this, cartItems, this);
        recyclerView.setAdapter(adapter);

        Button btnValidateOrder = findViewById(R.id.btnValidateOrder);
        btnValidateOrder.setOnClickListener(v -> showOrderForm());
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartItems.remove(item);
        adapter.notifyDataSetChanged();
    }

    private void showOrderForm() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_order_form, null);
        builder.setView(dialogView);

        EditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        EditText editLastName = dialogView.findViewById(R.id.editLastName);
        EditText editPhone = dialogView.findViewById(R.id.editPhone);
        EditText editAddress = dialogView.findViewById(R.id.editAddress);
        EditText editCity = dialogView.findViewById(R.id.editCity);
        TextView textDeliveryInfo = dialogView.findViewById(R.id.textDeliveryInfo);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();

        TextWatcher deliveryWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateDeliveryInfo(editCity.getText().toString().trim(), textDeliveryInfo);
            }
        };
        editCity.addTextChangedListener(deliveryWatcher);

        btnConfirm.setOnClickListener(v -> {
            String firstName = editFirstName.getText().toString().trim();
            String lastName = editLastName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String city = editCity.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            submitOrder(firstName, lastName, phone, address, city);
        });

        dialog.show();
    }

    private void updateDeliveryInfo(String city, TextView textView) {
        totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        String lowerCity = city.toLowerCase(Locale.ROOT);
        int deliveryFee;

        if (lowerCity.equals("casablanca")) {
            if (totalAmount >= 100) {
                textView.setText("Livraison gratuite à Casablanca");
                deliveryFee = 0;
            } else {
                deliveryFee = 10;
                textView.setText("Frais de livraison à Casablanca : 10 DH");
            }
        } else {
            if (totalAmount >= 1500) {
                textView.setText("Livraison gratuite hors Casablanca");
                deliveryFee = 0;
            } else {
                deliveryFee = 25;
                textView.setText("Frais de livraison hors Casablanca : 25 DH");
            }
        }
    }

    private void submitOrder(String firstName, String lastName, String phone, String address, String city) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vous devez être connecté pour valider une commande", Toast.LENGTH_LONG).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        List<Map<String, Object>> itemsMap = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getName());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("brand", item.getBrand());
            itemMap.put("bottleSize", item.getBottleSize());
            itemsMap.add(itemMap);
        }

        Map<String, Object> order = new HashMap<>();
        order.put("timestamp", new Date());
        order.put("email", userEmail);
        order.put("status", "en attente");
        order.put("firstName", firstName);
        order.put("lastName", lastName);
        order.put("phone", phone);
        order.put("address", address);
        order.put("city", city);
        order.put("total", totalAmount);
        order.put("items", itemsMap);

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    // Mise à jour du stock
                    for (CartItem item : cartItems) {
                        String productId = item.getId();
                        int orderedQty = item.getQuantity();

                        db.collection("products").document(productId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Long currentQty = documentSnapshot.getLong("quantity");
                                        if (currentQty != null) {
                                            long newQty = currentQty - orderedQty;
                                            if (newQty < 0) newQty = 0;

                                            db.collection("products").document(productId)
                                                    .update("quantity", newQty)
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Erreur de stock : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                });
                    }

                    Toast.makeText(this, "Commande validée", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().clearCart();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
