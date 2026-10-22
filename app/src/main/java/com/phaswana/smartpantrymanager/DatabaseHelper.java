package com.phaswana.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Pantry
    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Table: Recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_INSTRUCTIONS = "instructions";
    public static final String COL_RECIPE_PREP_TIME = "prep_time";
    public static final String COL_RECIPE_DIFFICULTY = "difficulty";

    // Table: Recipe Ingredients
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
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PANTRY_NAME + " TEXT NOT NULL, "
                + COL_PANTRY_QUANTITY + " REAL NOT NULL, "
                + COL_PANTRY_UNIT + " TEXT NOT NULL, "
                + COL_PANTRY_EXPIRY + " TEXT NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_NAME + " TEXT NOT NULL, "
                + COL_RECIPE_INSTRUCTIONS + " TEXT NOT NULL, "
                + COL_RECIPE_PREP_TIME + " INTEGER DEFAULT 15, "
                + COL_RECIPE_DIFFICULTY + " TEXT DEFAULT 'Easy');");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RI_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_RI_NAME + " TEXT NOT NULL, "
                + COL_RI_QUANTITY + " REAL NOT NULL, "
                + COL_RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE);");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // --- PANTRY CRUD ---

    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, item.getName().trim());
        cv.put(COL_PANTRY_QUANTITY, item.getQuantity());
        cv.put(COL_PANTRY_UNIT, item.getUnit());
        cv.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public List<PantryItem> getAllPantryItemsSortedByExpiry() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, COL_PANTRY_EXPIRY + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY));
                items.add(new PantryItem(id, name, quantity, unit, expiry));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, item.getName().trim());
        cv.put(COL_PANTRY_QUANTITY, item.getQuantity());
        cv.put(COL_PANTRY_UNIT, item.getUnit());
        cv.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        return db.update(TABLE_PANTRY, cv, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- RECIPES & MATCHING ENGINE ---

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT));
                list.add(new RecipeIngredient(id, recipeId, name, qty, unit));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Recipe> getAvailableRecipes() {
        List<Recipe> qualifiedRecipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor recipeCursor = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        if (recipeCursor != null && recipeCursor.moveToFirst()) {
            do {
                long recipeId = recipeCursor.getLong(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String name = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String instructions = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_INSTRUCTIONS));
                int prepTime = recipeCursor.getInt(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_PREP_TIME));
                String difficulty = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_DIFFICULTY));

                List<RecipeIngredient> required = getIngredientsForRecipe(recipeId);
                boolean canMake = !required.isEmpty();

                for (RecipeIngredient req : required) {
                    Cursor pantryCheck = db.rawQuery(
                            "SELECT SUM(" + COL_PANTRY_QUANTITY + ") FROM " + TABLE_PANTRY +
                            " WHERE LOWER(TRIM(" + COL_PANTRY_NAME + ")) = LOWER(TRIM(?))",
                            new String[]{req.getIngredientName()});

                    if (pantryCheck != null && pantryCheck.moveToFirst()) {
                        double availableQty = pantryCheck.getDouble(0);
                        pantryCheck.close();
                        if (availableQty < req.getRequiredQuantity()) {
                            canMake = false;
                            break;
                        }
                    } else {
                        if (pantryCheck != null) pantryCheck.close();
                        canMake = false;
                        break;
                    }
                }

                if (canMake) {
                    Recipe r = new Recipe(recipeId, name, instructions, prepTime, difficulty);
                    r.setIngredients(required);
                    qualifiedRecipes.add(r);
                }
            } while (recipeCursor.moveToNext());
            recipeCursor.close();
        }
        return qualifiedRecipes;
    }

    // --- SEED ENGINE ---

    private void seedInitialData(SQLiteDatabase db) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        // 1. Bread (expiring in 2 days)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 2);
        insertPantryDirect(db, "Bread", 6.0, "slices", sdf.format(cal.getTime()));

        // 2. Milk (expiring in 5 days)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 5);
        insertPantryDirect(db, "Milk", 500.0, "ml", sdf.format(cal.getTime()));

        // 3. Eggs (fresh, 14 days)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 14);
        insertPantryDirect(db, "Eggs", 6.0, "pcs", sdf.format(cal.getTime()));

        // 4. Butter (fresh, 30 days)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 30);
        insertPantryDirect(db, "Butter", 100.0, "g", sdf.format(cal.getTime()));

        // 5. Salt (stable staple, 180 days)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 180);
        insertPantryDirect(db, "Salt", 200.0, "g", sdf.format(cal.getTime()));

        // 6. Fresh Cream (expired yesterday to demonstrate the alert badge)
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        insertPantryDirect(db, "Fresh Cream", 100.0, "ml", sdf.format(cal.getTime()));

        // Seed 16 Recipes
        seedRecipes(db);
    }

    private void insertPantryDirect(SQLiteDatabase db, String name, double qty, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name);
        cv.put(COL_PANTRY_QUANTITY, qty);
        cv.put(COL_PANTRY_UNIT, unit);
        cv.put(COL_PANTRY_EXPIRY, expiry);
        db.insert(TABLE_PANTRY, null, cv);
    }

    private void seedRecipes(SQLiteDatabase db) {
        // Qualified Matches
        long r1 = insertRecipeDirect(db, "Butter Toast", "1. Toast bread slices until golden brown.\n2. Spread butter evenly across the warm surface.\n3. Serve warm immediately.", 5, "Easy");
        insertReq(db, r1, "Bread", 2.0, "slices");
        insertReq(db, r1, "Butter", 15.0, "g");

        long r2 = insertRecipeDirect(db, "Scrambled Eggs", "1. Whisk eggs, milk, and salt in a bowl.\n2. Melt butter in a non-stick pan over medium-low heat.\n3. Pour mixture and gently fold until softly set.", 8, "Easy");
        insertReq(db, r2, "Eggs", 2.0, "pcs");
        insertReq(db, r2, "Milk", 50.0, "ml");
        insertReq(db, r2, "Butter", 10.0, "g");
        insertReq(db, r2, "Salt", 2.0, "g");

        // Near Misses (1-2 items missing)
        long r3 = insertRecipeDirect(db, "French Toast", "1. Whisk egg, milk, sugar, and a pinch of salt.\n2. Dip bread slices into mixture.\n3. Fry in melted butter until both sides are golden.", 10, "Easy");
        insertReq(db, r3, "Bread", 2.0, "slices");
        insertReq(db, r3, "Eggs", 1.0, "pcs");
        insertReq(db, r3, "Milk", 60.0, "ml");
        insertReq(db, r3, "Butter", 15.0, "g");
        insertReq(db, r3, "Sugar", 10.0, "g");

        long r4 = insertRecipeDirect(db, "Sunny Side Up Egg", "1. Heat butter in a pan over medium heat.\n2. Crack egg gently into pan.\n3. Cook until whites are set and season with salt and pepper.", 5, "Easy");
        insertReq(db, r4, "Eggs", 1.0, "pcs");
        insertReq(db, r4, "Butter", 10.0, "g");
        insertReq(db, r4, "Salt", 1.0, "g");
        insertReq(db, r4, "Black Pepper", 1.0, "g");

        long r5 = insertRecipeDirect(db, "Classic Pancakes", "1. Whisk flour, sugar, baking powder, and salt.\n2. Stir in milk, melted butter, and egg until smooth.\n3. Ladle onto a griddle and flip when bubbly.", 20, "Medium");
        insertReq(db, r5, "Flour", 150.0, "g");
        insertReq(db, r5, "Milk", 200.0, "ml");
        insertReq(db, r5, "Eggs", 1.0, "pcs");
        insertReq(db, r5, "Butter", 20.0, "g");
        insertReq(db, r5, "Sugar", 25.0, "g");

        long r6 = insertRecipeDirect(db, "Cheese Omelette", "1. Beat eggs with salt and pepper.\n2. Pour into melted butter in skillet.\n3. Add grated cheese to one half, fold over, and serve.", 10, "Easy");
        insertReq(db, r6, "Eggs", 2.0, "pcs");
        insertReq(db, r6, "Butter", 10.0, "g");
        insertReq(db, r6, "Cheese", 40.0, "g");
        insertReq(db, r6, "Salt", 1.0, "g");

        long r7 = insertRecipeDirect(db, "Creamy Garlic Pasta", "1. Boil pasta in salted water.\n2. Sauté minced garlic in butter.\n3. Stir in cream and toss with drained pasta.", 25, "Medium");
        insertReq(db, r7, "Pasta", 200.0, "g");
        insertReq(db, r7, "Butter", 30.0, "g");
        insertReq(db, r7, "Garlic", 2.0, "cloves");
        insertReq(db, r7, "Salt", 5.0, "g");

        long r8 = insertRecipeDirect(db, "Tomato Herb Bruschetta", "1. Toast bread slices lightly.\n2. Dice fresh tomatoes and toss with olive oil, salt, and basil.\n3. Spoon generously over bread.", 15, "Easy");
        insertReq(db, r8, "Bread", 4.0, "slices");
        insertReq(db, r8, "Tomato", 2.0, "pcs");
        insertReq(db, r8, "Olive Oil", 15.0, "ml");
        insertReq(db, r8, "Salt", 2.0, "g");

        long r9 = insertRecipeDirect(db, "Egg Fried Rice", "1. Heat oil in a wok.\n2. Scramble eggs and set aside.\n3. Sauté cold cooked rice with soy sauce, salt, and fold eggs back in.", 15, "Easy");
        insertReq(db, r9, "Cooked Rice", 250.0, "g");
        insertReq(db, r9, "Eggs", 2.0, "pcs");
        insertReq(db, r9, "Cooking Oil", 15.0, "ml");
        insertReq(db, r9, "Salt", 2.0, "g");

        long r10 = insertRecipeDirect(db, "Avocado Toast", "1. Toast bread slices.\n2. Mash ripe avocado with salt and lemon juice.\n3. Spread over toast and top with chili flakes.", 8, "Easy");
        insertReq(db, r10, "Bread", 2.0, "slices");
        insertReq(db, r10, "Avocado", 1.0, "pcs");
        insertReq(db, r10, "Salt", 1.0, "g");

        long r11 = insertRecipeDirect(db, "Grilled Cheese Sandwich", "1. Place cheese slices between bread.\n2. Butter the outside slices.\n3. Toast in skillet until bread is crispy and cheese is melted.", 10, "Easy");
        insertReq(db, r11, "Bread", 2.0, "slices");
        insertReq(db, r11, "Cheese", 50.0, "g");
        insertReq(db, r11, "Butter", 15.0, "g");

        long r12 = insertRecipeDirect(db, "Tuna Melt", "1. Mix canned tuna with a dash of mayonnaise and salt.\n2. Spread on bread, top with cheese.\n3. Broil or toast until cheese bubbles.", 12, "Easy");
        insertReq(db, r12, "Bread", 2.0, "slices");
        insertReq(db, r12, "Tuna", 1.0, "can");
        insertReq(db, r12, "Cheese", 30.0, "g");

        long r13 = insertRecipeDirect(db, "Mashed Potatoes", "1. Boil peeled potatoes in salted water until tender.\n2. Drain and mash thoroughly.\n3. Fold in warm milk, butter, and extra salt to taste.", 25, "Medium");
        insertReq(db, r13, "Potato", 4.0, "pcs");
        insertReq(db, r13, "Butter", 30.0, "g");
        insertReq(db, r13, "Milk", 80.0, "ml");
        insertReq(db, r13, "Salt", 4.0, "g");

        long r14 = insertRecipeDirect(db, "Garlic Butter Naan", "1. Knead flour, yogurt, and milk into a soft dough.\n2. Roll thin and cook on blazing cast iron until puffed.\n3. Brush immediately with garlic butter.", 35, "Hard");
        insertReq(db, r14, "Flour", 200.0, "g");
        insertReq(db, r14, "Butter", 25.0, "g");
        insertReq(db, r14, "Garlic", 2.0, "cloves");
        insertReq(db, r14, "Milk", 50.0, "ml");

        long r15 = insertRecipeDirect(db, "Lentil Vegetable Soup", "1. Sauté onions, carrots, and garlic in oil.\n2. Add red lentils and water, simmer for 25 minutes.\n3. Season with salt and pepper before serving.", 35, "Medium");
        insertReq(db, r15, "Lentils", 150.0, "g");
        insertReq(db, r15, "Onion", 1.0, "pcs");
        insertReq(db, r15, "Garlic", 2.0, "cloves");
        insertReq(db, r15, "Salt", 5.0, "g");

        long r16 = insertRecipeDirect(db, "Chicken Stir Fry", "1. Slice chicken breast and sear in oil.\n2. Toss in vegetables and soy sauce.\n3. Cook on high heat for 6 minutes and serve.", 20, "Medium");
        insertReq(db, r16, "Chicken Breast", 250.0, "g");
        insertReq(db, r16, "Cooking Oil", 20.0, "ml");
        insertReq(db, r16, "Soy Sauce", 15.0, "ml");
        insertReq(db, r16, "Salt", 2.0, "g");
    }

    private long insertRecipeDirect(SQLiteDatabase db, String name, String instructions, int prepTime, String difficulty) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_INSTRUCTIONS, instructions);
        cv.put(COL_RECIPE_PREP_TIME, prepTime);
        cv.put(COL_RECIPE_DIFFICULTY, difficulty);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void insertReq(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QUANTITY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }
}
