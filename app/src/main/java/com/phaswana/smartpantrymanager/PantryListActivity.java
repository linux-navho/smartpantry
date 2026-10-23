package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PantryListActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView rvPantry;
    private PantryAdapter adapter;
    private List<PantryItem> pantryList;
    private DatabaseHelper dbHelper;
    private TextView tvEmptyPantry;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_pantry_list);

        dbHelper = new DatabaseHelper(this);
        pantryList = new ArrayList<>();

        rvPantry = findViewById(R.id.rvPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        bottomNav = findViewById(R.id.bottomNavigation);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddItem);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this, pantryList, this);
        rvPantry.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditItemActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        }
        loadPantryItems();
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recipes) {
                Intent intent = new Intent(this, SuggestedRecipesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return id == R.id.nav_pantry;
        });
    }

    private void loadPantryItems() {
        pantryList.clear();
        pantryList.addAll(dbHelper.getAllPantryItemsSortedByExpiry());
        adapter.notifyDataSetChanged();

        if (pantryList.isEmpty()) {
            rvPantry.setVisibility(View.GONE);
            if (tvEmptyPantry != null) {
                tvEmptyPantry.setVisibility(View.VISIBLE);
            }
        } else {
            rvPantry.setVisibility(View.VISIBLE);
            if (tvEmptyPantry != null) {
                tvEmptyPantry.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra("EXTRA_ITEM_ID", item.getId());
        intent.putExtra("EXTRA_ITEM_NAME", item.getName());
        intent.putExtra("EXTRA_ITEM_QUANTITY", item.getQuantity());
        intent.putExtra("EXTRA_ITEM_UNIT", item.getUnit());
        intent.putExtra("EXTRA_ITEM_EXPIRY", item.getExpiryDate());
        startActivity(intent);
    }
}
