package com.phaswana.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String instructions;
    private int prepTimeMinutes;
    private String difficulty;
    private List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String instructions, int prepTimeMinutes, String difficulty) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.prepTimeMinutes = prepTimeMinutes;
        this.difficulty = difficulty;
        this.ingredients = new ArrayList<>();
    }

    public Recipe(String name, String instructions, int prepTimeMinutes, String difficulty) {
        this(-1, name, instructions, prepTimeMinutes, difficulty);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public String getDifficulty() { return difficulty; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}
