package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Product;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etFilterBrand;
    private Button btnFilter;

    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        etFilterBrand = findViewById(R.id.etFilterBrand);
        btnFilter = findViewById(R.id.btnFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, allProducts, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadProducts();

        btnFilter.setOnClickListener(v -> {
            String brandFilter = etFilterBrand.getText().toString().trim().toLowerCase();
            if (brandFilter.isEmpty()) {
                adapter.updateList(allProducts);
            } else {
                List<Product> filtered = new ArrayList<>();
                for(Product p : allProducts) {
                    if(p.getBrand() != null && p.getBrand().toLowerCase().contains(brandFilter)) {
                        filtered.add(p);
                    }
                }
                adapter.updateList(filtered);
            }
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if(error != null) {
                        Toast.makeText(ProductListActivity.this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(value != null) {
                        allProducts.clear();
                        for(DocumentSnapshot doc : value.getDocuments()) {
                            Product p = doc.toObject(Product.class);
                            if(p != null){
                                p.setId(doc.getId());
                                allProducts.add(p);
                            }
                        }
                        adapter.updateList(allProducts);
                    }
                });
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer produit")
                .setMessage("Voulez-vous vraiment supprimer ce produit ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    db.collection("products").document(product.getId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(ProductListActivity.this, "Produit supprimé", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(ProductListActivity.this, "Erreur suppression", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public void onEdit(Product product) {
        // Rediriger vers l'écran d'édition (à créer), passer l'id produit en extra
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }
}
