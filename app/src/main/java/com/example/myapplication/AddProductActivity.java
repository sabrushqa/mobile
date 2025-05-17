package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtName, edtDescription, edtBrand, edtQuantity, edtPrice;
    private Spinner spinnerBottleSize, spinnerCategory;
    private Button btnSelectImage1, btnSelectImage2, btnSelectImage3, btnUploadProduct;

    private Uri imageUri1, imageUri2, imageUri3;
    private static final int IMAGE_PICK_1 = 1;
    private static final int IMAGE_PICK_2 = 2;
    private static final int IMAGE_PICK_3 = 3;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtBrand = findViewById(R.id.edtBrand);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);

        spinnerBottleSize = findViewById(R.id.spinnerBottleSize);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        btnSelectImage1 = findViewById(R.id.btnSelectImage1);
        btnSelectImage2 = findViewById(R.id.btnSelectImage2);
        btnSelectImage3 = findViewById(R.id.btnSelectImage3);
        btnUploadProduct = findViewById(R.id.btnUploadProduct);

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"100ml", "50ml", "30ml", "200ml"});
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBottleSize.setAdapter(sizeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Homme", "Femme", "Mixte"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        btnSelectImage1.setOnClickListener(v -> selectImage(IMAGE_PICK_1));
        btnSelectImage2.setOnClickListener(v -> selectImage(IMAGE_PICK_2));
        btnSelectImage3.setOnClickListener(v -> selectImage(IMAGE_PICK_3));

        btnUploadProduct.setOnClickListener(v -> uploadProduct());
    }

    private void selectImage(int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), code);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selected = data.getData();
            switch (requestCode) {
                case IMAGE_PICK_1:
                    imageUri1 = selected;
                    break;
                case IMAGE_PICK_2:
                    imageUri2 = selected;
                    break;
                case IMAGE_PICK_3:
                    imageUri3 = selected;
                    break;
            }
        }
    }

    private void uploadProduct() {
        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String brand = edtBrand.getText().toString().trim();
        String bottleSize = spinnerBottleSize.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || description.isEmpty() || brand.isEmpty() ||
                edtQuantity.getText().toString().isEmpty() || edtPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(edtQuantity.getText().toString());
            price = Double.parseDouble(edtPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantité et prix doivent être des nombres valides", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri1 == null || imageUri2 == null || imageUri3 == null) {
            Toast.makeText(this, "Veuillez sélectionner les 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Création du produit...");
        dialog.setCancelable(false);
        dialog.show();

        uploadImage(imageUri1, url1 ->
                uploadImage(imageUri2, url2 ->
                        uploadImage(imageUri3, url3 -> {
                            String id = UUID.randomUUID().toString();
                            List<String> imageUrls = Arrays.asList(url1, url2, url3);

                            Product product = new Product(id, name, description, imageUrls,
                                    quantity, brand, bottleSize, price, category);

                            db.collection("products").document(id).set(product)
                                    .addOnSuccessListener(unused -> {
                                        dialog.dismiss();
                                        Toast.makeText(this, "Produit créé avec succès", Toast.LENGTH_LONG).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                )
        );
    }

    private interface ImageUploadCallback {
        void onUploaded(String imageUrl);
    }

    private void uploadImage(Uri uri, ImageUploadCallback callback) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("product_images/" + fileName);
        ref.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uriResult -> callback.onUploaded(uriResult.toString()))
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur d’upload : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
