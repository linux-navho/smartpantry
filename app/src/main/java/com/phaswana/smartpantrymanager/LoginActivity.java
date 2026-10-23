package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Redundancy: Ensure window is definitely unlocked for Wayland/OBS
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);

        // If no account exists on the device, route to Register
        if (!authManager.isRegistered()) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // If already logged in this session, route to Pantry
        if (authManager.isLoggedIn()) {
            navigateToPantry();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);

        // Pre-fill username if they just registered
        String prefilled = getIntent().getStringExtra("EXTRA_PREFILLED_USER");
        if (prefilled != null && !prefilled.isEmpty()) {
            etUsername.setText(prefilled);
            etPassword.requestFocus();
        } else {
            etUsername.setText(authManager.getRegisteredUsername());
        }

        findViewById(R.id.btnSubmitLogin).setOnClickListener(v -> handleLogin());
        
        findViewById(R.id.tvGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Enter your username");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            return;
        }

        if (authManager.login(username, password)) {
            Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
            navigateToPantry();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPantry() {
        Intent intent = new Intent(this, PantryListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
