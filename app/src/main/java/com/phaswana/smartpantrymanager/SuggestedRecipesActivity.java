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
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvRecipes;
    private RecipeAdapter adapter;
    private TextView tvEmpty;
    private ChipGroup chipGroupFilter;
    private boolean showOnlyAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_suggested);
            setSupportActionBar(toolbar);
        }

        dbHelper = new DatabaseHelper(this);

        rvRecipes = findViewById(R.id.rvRecipes);
        tvEmpty = findViewById(R.id.tvEmptyRecipes);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, new ArrayList<>(), recipe -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("EXTRA_RECIPE_ID", recipe.getId());
            intent.putExtra("EXTRA_RECIPE_NAME", recipe.getName());
            intent.putExtra("EXTRA_RECIPE_INSTRUCTIONS", recipe.getInstructions());
            intent.putExtra("EXTRA_RECIPE_PREP_TIME", recipe.getPrepTimeMinutes());
            intent.putExtra("EXTRA_RECIPE_DIFFICULTY", recipe.getDifficulty());
            startActivity(intent);
        });
        rvRecipes.setAdapter(adapter);

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            showOnlyAvailable = (checkedId == R.id.chipAvailable);
            loadRecipes();
        });

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_recipes);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_pantry) {
                    Intent intent = new Intent(SuggestedRecipesActivity.this, PantryListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                }
                return itemId == R.id.nav_recipes;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_recipes);
        }
    }

    private void loadRecipes() {
        List<Recipe> list = dbHelper.getAllRecipesWithStockStatus(showOnlyAvailable);
        if (list.isEmpty()) {
            rvRecipes.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(showOnlyAvailable
                    ? "No recipes can be made with your current pantry stock.\nAdd ingredients to see suggestions!"
                    : "No recipes found.");
        } else {
            rvRecipes.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            adapter.updateList(list);
        }
    }
}
