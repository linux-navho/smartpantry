package com.phaswana.smartpantrymanager;

public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String ingredientName;
    private double requiredQuantity;
    private String unit;

    public RecipeIngredient(long id, long recipeId, String ingredientName, double requiredQuantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public RecipeIngredient(String ingredientName, double requiredQuantity, String unit) {
        this(-1, -1, ingredientName, requiredQuantity, unit);
    }

    public long getId() { return id; }
    public long getRecipeId() { return recipeId; }
    public String getIngredientName() { return ingredientName; }
    public double getRequiredQuantity() { return requiredQuantity; }
    public String getUnit() { return unit; }
}
