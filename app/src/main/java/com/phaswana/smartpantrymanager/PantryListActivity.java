package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvPantry;
    private PantryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_pantry);
            setSupportActionBar(toolbar);
        }

        dbHelper = new DatabaseHelper(this);

        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmptyPantry);
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this, new ArrayList<>(), item -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditItemActivity.class);
            intent.putExtra("EXTRA_ITEM_ID", item.getId());
            intent.putExtra("EXTRA_ITEM_NAME", item.getName());
            intent.putExtra("EXTRA_ITEM_QUANTITY", item.getQuantity());
            intent.putExtra("EXTRA_ITEM_UNIT", item.getUnit());
            intent.putExtra("EXTRA_ITEM_EXPIRY", item.getExpiryDate());
            startActivity(intent);
        });
        rvPantry.setAdapter(adapter);

        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditItemActivity.class);
            startActivity(intent);
        });

        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_recipes) {
                Intent intent = new Intent(PantryListActivity.this, SuggestedRecipesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return itemId == R.id.nav_pantry;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryData();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        }
    }

    private void loadPantryData() {
        List<PantryItem> items = dbHelper.getAllPantryItemsSortedByExpiry();
        if (items.isEmpty()) {
            rvPantry.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvPantry.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            adapter.updateList(items);
        }
    }
}
