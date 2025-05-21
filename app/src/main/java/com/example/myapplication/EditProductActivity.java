package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etBrand, etDescription, etPrice, etQuantity, etBottleSize, etCategory;
    private ImageView image1, image2, image3;
    private Button btnSave;

    private final List<ImageView> imageViews = new ArrayList<>();
    private final List<Uri> selectedImageUris = new ArrayList<>(Arrays.asList(null, null, null));
    private final List<String> existingImageUrls = new ArrayList<>(Arrays.asList("", "", ""));
    private int imageIndexToEdit = -1;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String productId;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageIndexToEdit != -1 && imageUri != null) {
                        selectedImageUris.set(imageIndexToEdit, imageUri);
                        Glide.with(this).load(imageUri).into(imageViews.get(imageIndexToEdit));
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Initialisation
        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etBottleSize = findViewById(R.id.etBottleSize);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);

        imageViews.add(image1);
        imageViews.add(image2);
        imageViews.add(image3);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Récupérer ID du produit
        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Produit introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Clics sur les images pour les modifier
        for (int i = 0; i < imageViews.size(); i++) {
            int index = i;
            imageViews.get(i).setOnClickListener(v -> {
                imageIndexToEdit = index;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        // Charger les données du produit
        db.collection("products").document(productId).get().addOnSuccessListener(documentSnapshot -> {
            Product product = documentSnapshot.toObject(Product.class);
            if (product != null) {
                etName.setText(product.getName());
                etBrand.setText(product.getBrand());
                etDescription.setText(product.getDescription());
                etPrice.setText(String.valueOf(product.getPrice()));
                etQuantity.setText(String.valueOf(product.getQuantity()));
                etBottleSize.setText(product.getBottleSize());
                etCategory.setText(product.getCategory());

                if (product.getImageUrls() != null) {
                    for (int i = 0; i < product.getImageUrls().size() && i < 3; i++) {
                        existingImageUrls.set(i, product.getImageUrls().get(i));
                        Glide.with(this).load(product.getImageUrls().get(i)).into(imageViews.get(i));
                    }
                }
            }
        });

        // Sauvegarde des modifications
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String bottleSize = etBottleSize.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (name.isEmpty() || brand.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Nom, marque et prix requis", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

        uploadImages(() -> {
            Product updatedProduct = new Product();
            updatedProduct.setName(name);
            updatedProduct.setBrand(brand);
            updatedProduct.setDescription(description);
            updatedProduct.setPrice(price);
            updatedProduct.setQuantity(quantity);
            updatedProduct.setBottleSize(bottleSize);
            updatedProduct.setCategory(category);
            updatedProduct.setImageUrls(existingImageUrls);

            db.collection("products").document(productId)
                    .set(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Produit modifié avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void uploadImages(Runnable onComplete) {
        List<Integer> toUpload = new ArrayList<>();
        for (int i = 0; i < selectedImageUris.size(); i++) {
            if (selectedImageUris.get(i) != null) {
                toUpload.add(i);
            }
        }

        if (toUpload.isEmpty()) {
            onComplete.run();
            return;
        }

        final int total = toUpload.size();
        final int[] uploaded = {0};

        for (int index : toUpload) {
            Uri uri = selectedImageUris.get(index);
            String fileName = "products/" + UUID.randomUUID() + ".jpg";
            storageRef.child(fileName).putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.child(fileName).getDownloadUrl()
                                    .addOnSuccessListener(downloadUri -> {
                                        existingImageUrls.set(index, downloadUri.toString());
                                        uploaded[0]++;
                                        if (uploaded[0] == total) onComplete.run();
                                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Échec de l’upload d’une image", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
