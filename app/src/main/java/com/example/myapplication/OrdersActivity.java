package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.MailSender;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ListView listViewOrders;
    private Spinner spinnerFilter;

    private final List<DocumentSnapshot> ordersList = new ArrayList<>();
    private ArrayAdapter<String> ordersAdapter;
    private ListenerRegistration listenerRegistration;

    private String currentFilter = "Tous les statuts";

    // SMTP credentials (à sécuriser en vrai)
    private static final String SMTP_EMAIL = "shperfumes7@gmail.com";
    private static final String SMTP_PASSWORD = "hwuuszhghouryknf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        firestore = FirebaseFirestore.getInstance();
        listViewOrders = findViewById(R.id.listViewOrders);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        setupSpinner();
        fetchOrders();
    }

    private void setupSpinner() {
        List<String> options = List.of("Tous les statuts", "en attente", "en traitement", "expédié");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                currentFilter = options.get(position);
                displayOrders();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void fetchOrders() {
        listenerRegistration = firestore.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Affiche les dernières commandes en haut
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots != null) {
                        ordersList.clear();
                        ordersList.addAll(snapshots.getDocuments());
                        displayOrders();
                    }
                });
    }

    private void displayOrders() {
        List<String> filtered = new ArrayList<>();

        for (DocumentSnapshot order : ordersList) {
            String status = order.getString("status");
            if (!"Tous les statuts".equals(currentFilter) && !currentFilter.equalsIgnoreCase(status)) {
                continue;
            }

            String emoji;
            if ("en attente".equalsIgnoreCase(status)) {
                emoji = "⏳";
            } else if ("en traitement".equalsIgnoreCase(status)) {
                emoji = "🔧";
            } else if ("expédié".equalsIgnoreCase(status)) {
                emoji = "📦";
            } else {
                emoji = "❓";
            }

            String email = order.getString("email");
            String orderId = order.getId();

            String line = emoji + " Commande #" + orderId +
                    "\n📧 " + email +
                    "\n📝 Statut : " + status;

            filtered.add(line);
        }

        ordersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtered);
        listViewOrders.setAdapter(ordersAdapter);

        listViewOrders.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot selectedOrder = getFilteredOrders().get(position);
            String status = selectedOrder.getString("status");

            if ("expédié".equalsIgnoreCase(status)) {
                Toast.makeText(this, "📦 Cette commande est déjà expédiée.", Toast.LENGTH_SHORT).show();
            } else {
                showStatusDialog(selectedOrder);
            }
        });
    }

    private List<DocumentSnapshot> getFilteredOrders() {
        List<DocumentSnapshot> result = new ArrayList<>();
        for (DocumentSnapshot order : ordersList) {
            String status = order.getString("status");
            if ("Tous les statuts".equals(currentFilter) || currentFilter.equalsIgnoreCase(status)) {
                result.add(order);
            }
        }
        return result;
    }

    private void showStatusDialog(DocumentSnapshot order) {
        String[] statuses = {"en attente", "en traitement", "expédié"};
        String currentStatus = order.getString("status");
        int checkedItem = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                checkedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔄 Changer le statut")
                .setSingleChoiceItems(statuses, checkedItem, null)
                .setPositiveButton("Valider", (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    int selectedPosition = alertDialog.getListView().getCheckedItemPosition();
                    String newStatus = statuses[selectedPosition];
                    updateOrderStatus(order.getId(), newStatus, order);
                })
                .setNegativeButton("Annuler", null)
                .setNeutralButton("Voir détails", (dialog, which) -> showOrderDetails(order))
                .show();
    }

    private void updateOrderStatus(String orderId, String newStatus, DocumentSnapshot order) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus);

        firestore.collection("orders").document(orderId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Statut mis à jour", Toast.LENGTH_SHORT).show();
                    sendEmailNotification(order, newStatus);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Méthode pour afficher les détails dans une simple AlertDialog
    private void showOrderDetails(DocumentSnapshot order) {
        StringBuilder details = new StringBuilder();

        details.append("Commande #").append(order.getId()).append("\n\n");
        details.append("Email: ").append(order.getString("email")).append("\n");
        details.append("Statut: ").append(order.getString("status")).append("\n\n");

        List<Map<String, Object>> products = (List<Map<String, Object>>) order.get("products");
        if (products != null && !products.isEmpty()) {
            details.append("Produits:\n");
            for (Map<String, Object> product : products) {
                details.append("- ").append(product.get("name")).append("\n");
            }
            details.append("\n");
        }

        details.append("Total: ").append(order.getDouble("total")).append(" DH\n\n");

        details.append("Informations de livraison:\n");
        details.append("Nom: ").append(order.getString("name")).append("\n");
        details.append("Téléphone: ").append(order.getString("phone")).append("\n");
        details.append("Adresse: ").append(order.getString("address")).append(", ").append(order.getString("city")).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Détails de la commande")
                .setMessage(details.toString())
                .setPositiveButton("Fermer", null)
                .show();
    }

    // Envoie un email de notification au client
    private void sendEmailNotification(DocumentSnapshot order, String newStatus) {
        String email = order.getString("email");
        List<Map<String, Object>> products = (List<Map<String, Object>>) order.get("products");
        double total = order.getDouble("total") != null ? order.getDouble("total") : 0.0;

        // Récupérer les noms de produits
        List<String> productNames = new ArrayList<>();
        if (products != null) {
            for (Map<String, Object> product : products) {
                String name = (String) product.get("name");
                if (name != null) {
                    productNames.add(name);
                }
            }
        }

        // Récupérer infos client
        String name = order.getString("name");
        String phone = order.getString("phone");
        String address = order.getString("address");
        String city = order.getString("city");

        String productsText = String.join(", ", productNames);
        String subject = "Mise à jour de votre commande #" + order.getId();
        String message = "Bonjour,\n\nVotre commande a été mise à jour.\n\n" +
                "Produits : " + productsText + "\n" +
                "Total : " + total + " DH\n" +
                "Nouveau statut : " + newStatus + "\n\n" +
                "Informations de livraison :\n" +
                "Nom : " + name + "\n" +
                "Téléphone : " + phone + "\n" +
                "Adresse : " + address + ", " + city + "\n\n" +
                "Merci pour votre confiance.";

        // Envoi dans un thread séparé
        new Thread(() -> {
            try {
                MailSender sender = new MailSender(SMTP_EMAIL, SMTP_PASSWORD);
                sender.sendMail(email, subject, message);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erreur email : " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
