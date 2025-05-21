package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.managers.CartManager;
import com.example.myapplication.models.CartItem;
import com.example.myapplication.models.Product;

import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvName, tvPrice, tvBrand, tvCategory, tvBottleSize, tvDescription;
    private ImageButton btnBack;
    private Button btnAddToCart;

    private Runnable autoScrollRunnable;
    private int currentPage = 0;

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialisation des vues
        viewPager = findViewById(R.id.detailViewPager);
        tvName = findViewById(R.id.detailProductName);
        tvPrice = findViewById(R.id.detailProductPrice);
        tvBrand = findViewById(R.id.detailProductBrand);
        tvCategory = findViewById(R.id.detailProductCategory);
        tvBottleSize = findViewById(R.id.detailProductBottleSize);
        tvDescription = findViewById(R.id.detailProductDescription);
        btnBack = findViewById(R.id.btnBack);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Récupération du produit depuis l'intent
        product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            // Affichage des infos
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%.2f MAD", product.getPrice()));
            tvBrand.setText("Marque : " + product.getBrand());
            tvCategory.setText("Catégorie : " + product.getCategory());
            tvBottleSize.setText("Volume : " + product.getBottleSize());
            tvDescription.setText(product.getDescription());

            // Images
            List<String> images = product.getImageUrls();
            if (images != null && !images.isEmpty()) {
                ImageSliderAdapter adapter = new ImageSliderAdapter(this, images);
                viewPager.setAdapter(adapter);

                // Auto-scroll
                autoScrollRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getItemCount() == 0) return;
                        currentPage = (currentPage + 1) % adapter.getItemCount();
                        viewPager.setCurrentItem(currentPage, true);
                        viewPager.postDelayed(this, 3000);
                    }
                };
                viewPager.postDelayed(autoScrollRunnable, 3000);
            }
        }

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Ajouter au panier
        btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                addToCart(product);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPager != null && autoScrollRunnable != null) {
            viewPager.removeCallbacks(autoScrollRunnable);
        }
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
