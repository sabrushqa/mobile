package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txtWelcomeAdmin;
    private Button btnLogout, btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAuth = FirebaseAuth.getInstance();

        txtWelcomeAdmin = findViewById(R.id.txtWelcomeAdmin);
        btnLogout = findViewById(R.id.btnLogoutAdmin);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            txtWelcomeAdmin.setText("Bienvenue Admin : " + currentUser.getEmail());
        }

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminHomeActivity.this, AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }
}
