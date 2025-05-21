package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE1 = 1;
    private static final int PICK_IMAGE2 = 2;
    private static final int PICK_IMAGE3 = 3;

    private Uri imageUri1, imageUri2, imageUri3;

    private ImageView imageView1, imageView2, imageView3;
    private Button btnChooseImage1, btnChooseImage2, btnChooseImage3, btnAddProduct;

    private EditText etName, etDescription, etPrice, etBrand, etBottleSize, etQuantity, etCategory;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etBrand = findViewById(R.id.etBrand);
        etBottleSize = findViewById(R.id.etBottleSize);
        etQuantity = findViewById(R.id.etQuantity);
        etCategory = findViewById(R.id.etCategory);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);

        btnChooseImage1 = findViewById(R.id.btnChooseImage1);
        btnChooseImage2 = findViewById(R.id.btnChooseImage2);
        btnChooseImage3 = findViewById(R.id.btnChooseImage3);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        btnChooseImage1.setOnClickListener(v -> openImagePicker(PICK_IMAGE1));
        btnChooseImage2.setOnClickListener(v -> openImagePicker(PICK_IMAGE2));
        btnChooseImage3.setOnClickListener(v -> openImagePicker(PICK_IMAGE3));

        btnAddProduct.setOnClickListener(v -> validateAndUpload());
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if(selectedImageUri == null) return;

            switch(requestCode) {
                case PICK_IMAGE1:
                    imageUri1 = selectedImageUri;
                    imageView1.setImageURI(imageUri1);
                    break;
                case PICK_IMAGE2:
                    imageUri2 = selectedImageUri;
                    imageView2.setImageURI(imageUri2);
                    break;
                case PICK_IMAGE3:
                    imageUri3 = selectedImageUri;
                    imageView3.setImageURI(imageUri3);
                    break;
            }
        }
    }

    private void validateAndUpload() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String bottleSize = etBottleSize.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if(name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || brand.isEmpty() ||
                bottleSize.isEmpty() || quantityStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Merci de remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if(imageUri1 == null || imageUri2 == null || imageUri3 == null) {
            Toast.makeText(this, "Merci de sélectionner les 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int quantity;

        try {
            price = Double.parseDouble(priceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prix ou quantité invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setBrand(brand);
        product.setBottleSize(bottleSize);
        product.setQuantity(quantity);
        product.setCategory(category);

        List<Uri> images = new ArrayList<>();
        images.add(imageUri1);
        images.add(imageUri2);
        images.add(imageUri3);

        uploadImagesAndAddProduct(product, images);
    }

    private void uploadImagesAndAddProduct(Product product, List<Uri> images) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("products_images");

        List<String> uploadedImageUrls = new ArrayList<>();
        final int totalImages = images.size();

        for(int i = 0; i < totalImages; i++) {
            Uri imageUri = images.get(i);
            String imageName = product.getName() + "_image" + (i + 1) + ".jpg";
            StorageReference imageRef = storageRef.child(imageName);

            int finalI = i;
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                uploadedImageUrls.add(uri.toString());
                                if (uploadedImageUrls.size() == totalImages) {
                                    product.setImageUrls(uploadedImageUrls);
                                    addProductToFirestore(product);
                                }
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("UploadImages", "Erreur upload image " + (finalI + 1), e);
                        Toast.makeText(AddProductActivity.this, "Erreur upload image " + (finalI + 1), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addProductToFirestore(Product product) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddProductActivity.this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // ferme l'activité après ajout
                })
                .addOnFailureListener(e -> {
                    Log.e("AddProduct", "Erreur ajout produit", e);
                    Toast.makeText(AddProductActivity.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                });
    }
}
