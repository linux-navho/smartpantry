package com.phaswana.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_recipe_detail);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView tvTitle = findViewById(R.id.tvRecipeDetailTitle);
        TextView tvMeta = findViewById(R.id.tvRecipeDetailMeta);
        TextView tvDifficulty = findViewById(R.id.tvRecipeDetailDifficulty);
        TextView tvIngredients = findViewById(R.id.tvRecipeDetailIngredients);
        TextView tvInstructions = findViewById(R.id.tvRecipeDetailInstructions);

        long recipeId = getIntent().getLongExtra("EXTRA_RECIPE_ID", -1);
        String name = getIntent().getStringExtra("EXTRA_RECIPE_NAME");
        String instructions = getIntent().getStringExtra("EXTRA_RECIPE_INSTRUCTIONS");
        int prepTime = getIntent().getIntExtra("EXTRA_RECIPE_PREP_TIME", 15);
        String difficulty = getIntent().getStringExtra("EXTRA_RECIPE_DIFFICULTY");

        if (name != null) tvTitle.setText(name);
        tvMeta.setText("⏱ " + prepTime + " mins");
        if (difficulty != null) tvDifficulty.setText(difficulty);
        if (instructions != null) tvInstructions.setText(instructions);

        if (recipeId != -1) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<RecipeIngredient> ingredients = dbHelper.getIngredientsForRecipe(recipeId);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ingredients.size(); i++) {
                RecipeIngredient item = ingredients.get(i);
                String qtyFormatted = (item.getRequiredQuantity() % 1 == 0)
                        ? String.valueOf((long) item.getRequiredQuantity())
                        : String.valueOf(item.getRequiredQuantity());
                sb.append("• ").append(item.getIngredientName()).append(" — ")
                  .append(qtyFormatted).append(" ").append(item.getUnit());
                if (i < ingredients.size() - 1) {
                    sb.append("\n");
                }
            }
            tvIngredients.setText(sb.toString());
        }
    }
}
