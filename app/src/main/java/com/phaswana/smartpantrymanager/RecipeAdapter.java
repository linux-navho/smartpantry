package com.phaswana.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final Context context;
    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    public void updateList(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvTitle.setText(recipe.getName());
        holder.tvPrepTime.setText("⏱ " + recipe.getPrepTimeMinutes() + " mins");
        holder.tvDifficulty.setText(recipe.getDifficulty());

        int count = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
        holder.tvIngredientCount.setText(count + " ingredients required");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvPrepTime;
        final TextView tvDifficulty;
        final TextView tvIngredientCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeCardTitle);
            tvPrepTime = itemView.findViewById(R.id.tvRecipePrepTime);
            tvDifficulty = itemView.findViewById(R.id.tvRecipeDifficulty);
            tvIngredientCount = itemView.findViewById(R.id.tvRecipeIngredientCount);
        }
    }
}
