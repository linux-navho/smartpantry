package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SmartPantryPrefs";
    private static final String KEY_PANTRY_NAME = "pantry_name";
    private static final String KEY_WARNING_DAYS = "warning_days";

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private AuthManager authManager;

    private TextView tvAvatarCircle, tvPantryProfileName;
    private TextView tvStatTotalItems, tvStatTotalRecipes;
    private ChipGroup chipGroupThreshold;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        authManager = new AuthManager(this);

        tvAvatarCircle = findViewById(R.id.tvAvatarCircle);
        tvPantryProfileName = findViewById(R.id.tvPantryProfileName);
        tvStatTotalItems = findViewById(R.id.tvStatTotalItems);
        tvStatTotalRecipes = findViewById(R.id.tvStatTotalRecipes);
        chipGroupThreshold = findViewById(R.id.chipGroupThreshold);
        bottomNav = findViewById(R.id.bottomNavigation);

        setupBottomNavigation();
        loadProfileData();
        setupThresholdChips();
        loadDatabaseStats();

        findViewById(R.id.btnEditProfileName).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.btnResetDatabase).setOnClickListener(v -> confirmDatabaseReset());

        TextView btnLogout = findViewById(R.id.tvHouseholdSubtitle);
        if (btnLogout != null) {
            btnLogout.setText("User: " + authManager.getRegisteredUsername() + " • Tap to Log Out");
            btnLogout.setClickable(true);
            btnLogout.setFocusable(true);
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);
        }
        loadDatabaseStats();
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                Intent intent = new Intent(this, PantryListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_recipes) {
                Intent intent = new Intent(this, SuggestedRecipesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return id == R.id.nav_settings;
        });
    }

    private void loadProfileData() {
        String savedName = prefs.getString(KEY_PANTRY_NAME, authManager.getRegisteredUsername() + "'s Kitchen");
        tvPantryProfileName.setText(savedName);
        if (!savedName.isEmpty()) {
            tvAvatarCircle.setText(savedName.substring(0, 1).toUpperCase());
        }
    }

    private void setupThresholdChips() {
        int savedDays = prefs.getInt(KEY_WARNING_DAYS, 3);
        if (savedDays == 2) chipGroupThreshold.check(R.id.chip2Days);
        else if (savedDays == 5) chipGroupThreshold.check(R.id.chip5Days);
        else if (savedDays == 7) chipGroupThreshold.check(R.id.chip7Days);
        else chipGroupThreshold.check(R.id.chip3Days);

        chipGroupThreshold.setOnCheckedChangeListener((group, checkedId) -> {
            int days = 3;
            if (checkedId == R.id.chip2Days) days = 2;
            else if (checkedId == R.id.chip5Days) days = 5;
            else if (checkedId == R.id.chip7Days) days = 7;

            prefs.edit().putInt(KEY_WARNING_DAYS, days).apply();
            Toast.makeText(this, "Alert threshold set to " + days + " days", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDatabaseStats() {
        List<PantryItem> pantryList = dbHelper.getAllPantryItemsSortedByExpiry();
        List<Recipe> recipeList = dbHelper.getAllRecipesWithStockStatus(false);

        tvStatTotalItems.setText(pantryList.size() + " items");
        tvStatTotalRecipes.setText(recipeList.size() + " recipes");
    }

    private void showEditProfileDialog() {
        EditText input = new EditText(this);
        input.setText(tvPantryProfileName.getText());
        input.setSelection(input.getText().length());
        input.setTextColor(getResources().getColor(R.color.text_primary));

        new AlertDialog.Builder(this)
                .setTitle("Pantry Profile Name")
                .setMessage("Enter a custom name for your local kitchen:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        prefs.edit().putString(KEY_PANTRY_NAME, newName).apply();
                        loadProfileData();
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDatabaseReset() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Demo Data")
                .setMessage("This will wipe all custom pantry items and restore the initial sample kitchen. Proceed?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    dbHelper.resetAndReseedDatabase();
                    loadDatabaseStats();
                    Toast.makeText(this, "Database re-seeded with sample data", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to sign out of your kitchen?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    authManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
