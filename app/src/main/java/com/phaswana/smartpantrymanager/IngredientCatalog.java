package com.phaswana.smartpantrymanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IngredientCatalog {

    public static class Spec {
        public final String unit;
        public final double step;
        public final double defaultValue;
        public final double[] presets;

        public Spec(String unit, double step, double defaultValue, double[] presets) {
            this.unit = unit;
            this.step = step;
            this.defaultValue = defaultValue;
            this.presets = presets;
        }
    }

    private static final Map<String, Spec> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("Avocado", new Spec("pcs", 1.0, 1.0, new double[]{1, 2, 4}));
        CATALOG.put("Black Pepper", new Spec("g", 5.0, 10.0, new double[]{5, 10, 25}));
        CATALOG.put("Bread", new Spec("slices", 2.0, 4.0, new double[]{2, 4, 6, 8}));
        CATALOG.put("Butter", new Spec("g", 25.0, 100.0, new double[]{50, 100, 250}));
        CATALOG.put("Cheese", new Spec("g", 25.0, 100.0, new double[]{50, 100, 200}));
        CATALOG.put("Chicken Breast", new Spec("g", 50.0, 250.0, new double[]{200, 300, 500}));
        CATALOG.put("Cooked Rice", new Spec("g", 50.0, 200.0, new double[]{150, 250, 500}));
        CATALOG.put("Cooking Oil", new Spec("ml", 25.0, 100.0, new double[]{50, 100, 250}));
        CATALOG.put("Eggs", new Spec("pcs", 1.0, 6.0, new double[]{2, 4, 6, 12}));
        CATALOG.put("Flour", new Spec("g", 50.0, 250.0, new double[]{100, 250, 500}));
        CATALOG.put("Fresh Cream", new Spec("ml", 50.0, 150.0, new double[]{100, 200, 250}));
        CATALOG.put("Garlic", new Spec("cloves", 1.0, 4.0, new double[]{2, 4, 6}));
        CATALOG.put("Lentils", new Spec("g", 50.0, 200.0, new double[]{100, 200, 500}));
        CATALOG.put("Milk", new Spec("ml", 50.0, 250.0, new double[]{100, 250, 500, 1000}));
        CATALOG.put("Olive Oil", new Spec("ml", 25.0, 100.0, new double[]{50, 100, 250}));
        CATALOG.put("Onion", new Spec("pcs", 1.0, 2.0, new double[]{1, 2, 4}));
        CATALOG.put("Pasta", new Spec("g", 50.0, 250.0, new double[]{200, 300, 500}));
        CATALOG.put("Potato", new Spec("pcs", 1.0, 4.0, new double[]{2, 4, 6}));
        CATALOG.put("Salt", new Spec("g", 10.0, 100.0, new double[]{25, 50, 100, 250}));
        CATALOG.put("Soy Sauce", new Spec("ml", 15.0, 50.0, new double[]{30, 50, 100}));
        CATALOG.put("Sugar", new Spec("g", 25.0, 100.0, new double[]{50, 100, 250}));
        CATALOG.put("Tomato", new Spec("pcs", 1.0, 2.0, new double[]{2, 4, 6}));
        CATALOG.put("Tuna", new Spec("can", 1.0, 1.0, new double[]{1, 2, 3}));
    }

    public static List<String> getNames() {
        List<String> list = new ArrayList<>(CATALOG.keySet());
        Collections.sort(list);
        return list;
    }

    public static Spec getSpec(String name) {
        if (name == null) return null;
        for (Map.Entry<String, Spec> entry : CATALOG.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name.trim())) {
                return entry.getValue();
            }
        }
        return new Spec("pcs", 1.0, 1.0, new double[]{1, 2, 5});
    }
}
