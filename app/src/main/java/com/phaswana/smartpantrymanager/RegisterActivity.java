package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Redundancy: Ensure window is definitely unlocked for Wayland/OBS
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new AuthManager(this);

        etUsername = findViewById(R.id.etRegisterUsername);
        etPassword = findViewById(R.id.etRegisterPassword);

        findViewById(R.id.btnSubmitRegister).setOnClickListener(v -> handleRegister());
        
        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Choose a username");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Create a password");
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            return;
        }

        // Save credentials to SharedPreferences via AuthManager
        authManager.register(username, password);
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

        // Send them back to Login, pre-filling the username they just created
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("EXTRA_PREFILLED_USER", username);
        startActivity(intent);
        finish();
    }
}
