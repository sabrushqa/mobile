package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    // Remplace ces identifiants par les bons
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtAdminEmail);
        edtPassword = findViewById(R.id.edtAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);

        btnLogin.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();



            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, AdminHomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
