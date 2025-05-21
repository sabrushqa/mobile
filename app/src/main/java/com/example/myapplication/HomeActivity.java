package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.managers.CartManager;
import com.example.myapplication.models.CartItem;
import com.example.myapplication.models.Product;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView emailTextView;
    private Button btnSignOut, btnViewCart;
    private RecyclerView recyclerViewProducts;
    private AdapterProduct adapterProduct;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private Button btnClientOrders ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build());

        emailTextView = findViewById(R.id.txtEmail);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnViewCart = findViewById(R.id.btnViewCart);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            emailTextView.setText("Connecté en tant que : " + user.getEmail());
        }

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        adapterProduct = new AdapterProduct(this, productList, this::addToCart);
        recyclerViewProducts.setAdapter(adapterProduct);

        loadProductsFromFirestore();

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            });
        });

        btnViewCart.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });



    }

    private void loadProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            productList.add(p);
                        }
                    }
                    adapterProduct.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Erreur chargement produits", Toast.LENGTH_SHORT).show());
    }

    private void addToCart(Product product) {
        CartItem item = new CartItem();
        item.setId(product.getId());
        item.setName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(1);
        item.setBrand(product.getBrand());
        item.setBottleSize(product.getBottleSize());
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            item.setImageUrl(product.getImageUrls().get(0));
        }

        CartManager.getInstance().addItem(item);
        Toast.makeText(this, "Produit ajouté au panier", Toast.LENGTH_SHORT).show();
    }
}
