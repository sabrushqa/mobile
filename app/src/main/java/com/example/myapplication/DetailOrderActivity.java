package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DetailOrderActivity extends AppCompatActivity {

    private TextView textFullName, textEmail, textPhone, textCity, textAddress, textTotal, textStatus, textTimestamp;
    private LinearLayout productsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Initialisation des vues
        textFullName = findViewById(R.id.textFullName);
        textEmail = findViewById(R.id.textEmail);
        textPhone = findViewById(R.id.textPhone);
        textCity = findViewById(R.id.textCity);
        textAddress = findViewById(R.id.textAddress);
        textTotal = findViewById(R.id.textTotal);
        textStatus = findViewById(R.id.textStatus);
        textTimestamp = findViewById(R.id.textTimestamp);
        productsContainer = findViewById(R.id.productsContainer);

        // Récupération de l'objet Order
        Order order = (Order) getIntent().getSerializableExtra("order");

        if (order == null) {
            Log.e("DetailOrderActivity", "Order est null !");
            textFullName.setText("Erreur : commande non trouvée");
            return;
        }

        // Affichage des infos
        textFullName.setText(order.getFullName());
        textEmail.setText(order.getEmail());
        textPhone.setText(order.getPhone());
        textCity.setText(order.getCity());
        textAddress.setText(order.getAddress());
        textTotal.setText("Total : " + order.getTotal() + " €");
        textStatus.setText("Statut : " + order.getStatus());

        // Formater la date
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(order.getTimestamp()));
        textTimestamp.setText("Date : " + formattedDate);

        // Affichage des produits
        Map<String, Object> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                String productName = entry.getKey();
                Object value = entry.getValue();

                int quantity = 1;
                if (value instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    Object q = valueMap.get("quantity");
                    if (q instanceof Number) {
                        quantity = ((Number) q).intValue();
                    }
                }

                TextView productView = new TextView(this);
                productView.setText("- " + productName + " x" + quantity);
                productsContainer.addView(productView);
            }
        } else {
            TextView noProductsView = new TextView(this);
            noProductsView.setText("Aucun produit trouvé.");
            productsContainer.addView(noProductsView);
        }
    }
}
