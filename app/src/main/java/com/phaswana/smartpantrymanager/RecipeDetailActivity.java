package com.phaswana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long recipeId = -1;
    private double currentScale = 1.0;
    private List<RecipeIngredientDetail> ingredientDetails;

    private TextView tvReadiness, tvInstructions;
    private LinearLayout containerIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);

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
        TextView tvPrep = findViewById(R.id.tvRecipeDetailPrep);
        TextView tvDifficulty = findViewById(R.id.tvRecipeDetailDifficulty);
        tvReadiness = findViewById(R.id.tvRecipeReadiness);
        tvInstructions = findViewById(R.id.tvRecipeDetailInstructions);
        containerIngredients = findViewById(R.id.containerIngredients);
        ChipGroup chipGroupServings = findViewById(R.id.chipGroupServings);

        recipeId = getIntent().getLongExtra("EXTRA_RECIPE_ID", -1);
        String name = getIntent().getStringExtra("EXTRA_RECIPE_NAME");
        String instructions = getIntent().getStringExtra("EXTRA_RECIPE_INSTRUCTIONS");
        int prepTime = getIntent().getIntExtra("EXTRA_RECIPE_PREP_TIME", 15);
        String difficulty = getIntent().getStringExtra("EXTRA_RECIPE_DIFFICULTY");

        if (name != null) tvTitle.setText(name);
        tvPrep.setText(prepTime + " mins");
        if (difficulty != null) tvDifficulty.setText(difficulty);
        if (instructions != null) tvInstructions.setText(instructions);

        chipGroupServings.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipScale2) {
                currentScale = 2.0;
            } else {
                currentScale = 1.0;
            }
            renderIngredients();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recipeId != -1) {
            ingredientDetails = dbHelper.getRecipeIngredientsWithLiveStock(recipeId);
            renderIngredients();
        }
    }

    private void renderIngredients() {
        if (ingredientDetails == null) return;
        containerIngredients.removeAllViews();

        int readyCount = 0;
        int totalCount = ingredientDetails.size();

        for (RecipeIngredientDetail item : ingredientDetails) {
            double scaledReq = item.getRequiredQuantity() * currentScale;
            boolean hasEnough = item.getAvailableQuantity() >= scaledReq;
            boolean partial = item.getAvailableQuantity() > 0 && !hasEnough;

            if (hasEnough) readyCount++;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.card_surface_background);
            card.setPadding(32, 28, 32, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            String scaledReqStr = (scaledReq % 1 == 0) ? String.valueOf((long) scaledReq) : String.valueOf(scaledReq);
            tvName.setText(item.getName() + " (" + scaledReqStr + " " + item.getUnit() + ")");
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvName.setTextSize(15);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            topRow.addView(tvName, nameLp);

            TextView tvBadge = new TextView(this);
            tvBadge.setPadding(20, 6, 20, 6);
            tvBadge.setTextSize(11);
            tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSubtext = new TextView(this);
            tvSubtext.setTextSize(12);
            tvSubtext.setPadding(0, 8, 0, 0);

            String availStr = (item.getAvailableQuantity() % 1 == 0)
                    ? String.valueOf((long) item.getAvailableQuantity())
                    : String.valueOf(item.getAvailableQuantity());

            if (hasEnough) {
                tvBadge.setText("✓ In Stock");
                tvBadge.setBackgroundResource(R.drawable.badge_fresh);
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_fresh_text));
                tvSubtext.setText("Have " + availStr + " " + item.getUnit() + " in pantry");
                tvSubtext.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            } else if (partial) {
                double deficit = scaledReq - item.getAvailableQuantity();
                String defStr = (deficit % 1 == 0) ? String.valueOf((long) deficit) : String.valueOf(deficit);
                tvBadge.setText("⚠ Need " + defStr + " " + item.getUnit());
                tvBadge.setBackgroundResource(R.drawable.badge_warning);
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
                tvSubtext.setText("Have " + availStr + " " + item.getUnit() + " — short by " + defStr + " " + item.getUnit());
                tvSubtext.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
            } else {
                tvBadge.setText("✕ Missing");
                tvBadge.setBackgroundResource(R.drawable.badge_expired);
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
                tvSubtext.setText("0 " + item.getUnit() + " in pantry. Tap to restock.");
                tvSubtext.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
            }

            topRow.addView(tvBadge);
            card.addView(topRow);
            card.addView(tvSubtext);

            if (!hasEnough) {
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(RecipeDetailActivity.this, AddEditItemActivity.class);
                    double def = Math.max(1.0, scaledReq - item.getAvailableQuantity());
                    intent.putExtra("EXTRA_ITEM_NAME", item.getName());
                    intent.putExtra("EXTRA_ITEM_QUANTITY", def);
                    startActivity(intent);
                });
            }

            containerIngredients.addView(card);
        }

        if (readyCount == totalCount) {
            tvReadiness.setText("100% Ready (" + readyCount + "/" + totalCount + ")");
            tvReadiness.setTextColor(ContextCompat.getColor(this, R.color.status_fresh_text));
        } else {
            tvReadiness.setText(readyCount + " of " + totalCount + " Available");
            tvReadiness.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
        }
    }
}
