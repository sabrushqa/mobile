package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    // ⚠️ Remplace ceci par l’email de ton admin
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtAdminEmail);
        edtPassword = findViewById(R.id.edtAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);

        btnLogin.setOnClickListener(view -> loginAdmin());
    }

    private void loginAdmin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ✅ Vérification des champs
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Adresse email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Vérifie si c’est bien l'email admin
        if (!email.equalsIgnoreCase(ADMIN_EMAIL)) {
            Toast.makeText(this, "Accès refusé : email non autorisé", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Connexion Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, AdminHomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Échec de connexion : vérifiez vos identifiants", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
