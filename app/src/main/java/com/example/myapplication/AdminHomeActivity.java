package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

public class AdminHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txtWelcomeAdmin, txtOrderStats;
    private Button btnLogout, btnAddProduct, btnViewProducts, btnViewOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAuth = FirebaseAuth.getInstance();

        txtWelcomeAdmin = findViewById(R.id.txtWelcomeAdmin);
        txtOrderStats = findViewById(R.id.txtOrderStats);
        btnLogout = findViewById(R.id.btnLogoutAdmin);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnViewProducts = findViewById(R.id.btnViewProducts);
        btnViewOrders = findViewById(R.id.btnViewOrders);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            txtWelcomeAdmin.setText("👋 Bienvenue Admin : " + currentUser.getEmail());
        }

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        btnViewProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductListActivity.class)));
        btnViewOrders.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));

        loadStatsAndLowStock();
    }

    private void loadStatsAndLowStock() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Commandes du mois
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = calendar.getTime();

        db.collection("orders")
                .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    int count = orderSnapshots.size();

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    // Commandes ce mois
                    String ordersLine = "📅 Commandes ce mois : " + count + "\n\n";
                    builder.append(ordersLine);

                    // 2. Produits en stock faible
                    db.collection("products")
                            .whereLessThan("quantity", 5)
                            .get()
                            .addOnSuccessListener(productSnapshots -> {
                                if (!productSnapshots.isEmpty()) {
                                    builder.append("⚠️ Produits en stock faible :\n");
                                    for (QueryDocumentSnapshot doc : productSnapshots) {
                                        String name = doc.getString("name");
                                        String bottleSize = doc.getString("bottleSize");
                                        Long quantity = doc.getLong("quantity");

                                        String line = "• " + name + " (" + bottleSize + ") - Qté: " + quantity + "\n";

                                        Spannable span = new SpannableStringBuilder(line);
                                        span.setSpan(new ForegroundColorSpan(Color.RED), 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        builder.append(span);
                                    }
                                } else {
                                    builder.append("✅ Tous les produits ont un stock suffisant.");
                                }

                                txtOrderStats.setText(builder);
                            })
                            .addOnFailureListener(e -> {
                                builder.append("\n❌ Erreur chargement produits.");
                                txtOrderStats.setText(builder);
                            });

                })
                .addOnFailureListener(e -> txtOrderStats.setText("❌ Erreur chargement commandes."));
    }
}
