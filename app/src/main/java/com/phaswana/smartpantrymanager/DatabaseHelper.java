package com.phaswana.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_INSTRUCTIONS = "instructions";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "required_quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT UNIQUE, " +
                COL_PANTRY_QUANTITY + " REAL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT)";
        db.execSQL(createPantryTable);

        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT, " +
                COL_RECIPE_INSTRUCTIONS + " TEXT)";
        db.execSQL(createRecipesTable);

        String createRecipeIngredientsTable = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER, " +
                COL_RI_NAME + " TEXT, " +
                COL_RI_QUANTITY + " REAL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE)";
        db.execSQL(createRecipeIngredientsTable);

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    public void resetAndReseedDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DELETE FROM " + TABLE_RECIPES);
        db.execSQL("DELETE FROM " + TABLE_PANTRY);
        seedInitialData(db);
    }

    public void seedInitialData(SQLiteDatabase db) {
        insertPantryItemDirect(db, "Eggs", 6.0, "pcs", "2026-11-15");
        insertPantryItemDirect(db, "Milk", 1.0, "L", "2026-10-28");
        insertPantryItemDirect(db, "Bread", 1.0, "loaf", "2026-10-25");
        insertPantryItemDirect(db, "Butter", 250.0, "g", "2026-12-10");
        insertPantryItemDirect(db, "Flour", 2.0, "kg", "2027-02-01");
        insertPantryItemDirect(db, "Sugar", 1.0, "kg", "2027-05-01");
        insertPantryItemDirect(db, "Salt", 500.0, "g", "2027-01-01");
        insertPantryItemDirect(db, "Fresh Cream", 200.0, "ml", "2026-10-21");

        long r1 = insertRecipe(db, "Scrambled Eggs", "Whisk eggs with a splash of milk and salt. Melt butter in a pan over medium heat, pour in mixture, and stir gently until soft curds form.");
        insertRecipeIngredient(db, r1, "Eggs", 2.0, "pcs");
        insertRecipeIngredient(db, r1, "Milk", 50.0, "ml");
        insertRecipeIngredient(db, r1, "Butter", 20.0, "g");
        insertRecipeIngredient(db, r1, "Salt", 2.0, "g");

        long r2 = insertRecipe(db, "Butter Toast", "Toast the bread slices until golden brown. Spread generous amounts of butter evenly across each warm slice.");
        insertRecipeIngredient(db, r2, "Bread", 2.0, "slices");
        insertRecipeIngredient(db, r2, "Butter", 15.0, "g");

        long r3 = insertRecipe(db, "Pancakes", "Mix flour, sugar, milk, and an egg into a smooth batter. Cook ladlefuls on a hot greased pan until bubbles appear, then flip.");
        insertRecipeIngredient(db, r3, "Flour", 200.0, "g");
        insertRecipeIngredient(db, r3, "Sugar", 30.0, "g");
        insertRecipeIngredient(db, r3, "Milk", 150.0, "ml");
        insertRecipeIngredient(db, r3, "Eggs", 1.0, "pcs");

        long r4 = insertRecipe(db, "Creamy Garlic Pasta", "Boil pasta. Sauté garlic in butter, pour in fresh cream, simmer until thickened, and toss with salt.");
        insertRecipeIngredient(db, r4, "Fresh Cream", 100.0, "ml");
        insertRecipeIngredient(db, r4, "Butter", 30.0, "g");
        insertRecipeIngredient(db, r4, "Salt", 5.0, "g");
    }

    private void insertPantryItemDirect(SQLiteDatabase db, String name, double qty, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name);
        cv.put(COL_PANTRY_QUANTITY, qty);
        cv.put(COL_PANTRY_UNIT, unit);
        cv.put(COL_PANTRY_EXPIRY, expiry);
        db.insertWithOnConflict(TABLE_PANTRY, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, item.getName());
        cv.put(COL_PANTRY_QUANTITY, item.getQuantity());
        cv.put(COL_PANTRY_UNIT, item.getUnit());
        cv.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        db.insertWithOnConflict(TABLE_PANTRY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, item.getName());
        cv.put(COL_PANTRY_QUANTITY, item.getQuantity());
        cv.put(COL_PANTRY_UNIT, item.getUnit());
        cv.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        db.update(TABLE_PANTRY, cv, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " WHERE LOWER(TRIM(" + COL_PANTRY_NAME + ")) = LOWER(TRIM(?))", new String[]{name});
        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = new PantryItem(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_PANTRY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
            );
        }
        cursor.close();
        return item;
    }

    private long insertRecipe(SQLiteDatabase db, String name, String instructions) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_INSTRUCTIONS, instructions);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QUANTITY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }

    public List<PantryItem> getAllPantryItemsSortedByExpiry() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COL_PANTRY_EXPIRY + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                PantryItem item = new PantryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PANTRY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
                );
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Recipe> getAllRecipesWithStockStatus(boolean onlyAvailable) {
        List<Recipe> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor rCursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);
        if (rCursor.moveToFirst()) {
            do {
                long recipeId = rCursor.getLong(rCursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String recipeName = rCursor.getString(rCursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String instructions = rCursor.getString(rCursor.getColumnIndexOrThrow(COL_RECIPE_INSTRUCTIONS));

                List<RecipeIngredient> ingredients = getIngredientsForRecipe(db, recipeId);
                int missingCount = 0;

                for (RecipeIngredient req : ingredients) {
                    double availableQty = getAvailableStockForIngredient(db, req.getIngredientName());
                    if (availableQty < req.getRequiredQuantity()) {
                        missingCount++;
                    }
                }

                boolean canMake = (missingCount == 0 && !ingredients.isEmpty());

                if (!onlyAvailable || canMake) {
                    // Create Recipe with defaults for prepTime (15m) and difficulty ("Easy")
                    Recipe recipe = new Recipe(recipeId, recipeName, instructions, 15, "Easy");
                    // Use setters to inject the dynamic inventory context
                    recipe.setAvailable(canMake);
                    recipe.setMissingIngredientsCount(missingCount);
                    recipe.setIngredients(ingredients);
                    
                    results.add(recipe);
                }
            } while (rCursor.moveToNext());
        }
        rCursor.close();
        return results;
    }

    public List<RecipeIngredientDetail> getRecipeIngredientsWithLiveStock(long recipeId) {
        List<RecipeIngredientDetail> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        List<RecipeIngredient> reqs = getIngredientsForRecipe(db, recipeId);

        for (RecipeIngredient req : reqs) {
            double available = getAvailableStockForIngredient(db, req.getIngredientName());
            list.add(new RecipeIngredientDetail(
                req.getIngredientName(),
                req.getRequiredQuantity(),
                available,
                req.getUnit()
            ));
        }
        return list;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE " + COL_RI_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new RecipeIngredient(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private double getAvailableStockForIngredient(SQLiteDatabase db, String name) {
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_PANTRY_QUANTITY + ") FROM " + TABLE_PANTRY + " WHERE LOWER(TRIM(" + COL_PANTRY_NAME + ")) = LOWER(TRIM(?))", new String[]{name});
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
}
