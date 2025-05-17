package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        findViewById(R.id.btnClient).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)); // Google Sign-In
        });

        findViewById(R.id.btnAdmin).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class)); // Email/Password
        });
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }
}
