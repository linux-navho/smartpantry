package com.phaswana.smartpantrymanager;

public class RecipeIngredientDetail {
    public enum StockStatus {
        IN_STOCK,
        PARTIAL,
        MISSING
    }

    private final String name;
    private final double requiredQuantity;
    private final double availableQuantity;
    private final String unit;
    private final StockStatus status;

    public RecipeIngredientDetail(String name, double requiredQuantity, double availableQuantity, String unit) {
        this.name = name;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
        this.unit = unit;

        if (availableQuantity >= requiredQuantity) {
            this.status = StockStatus.IN_STOCK;
        } else if (availableQuantity > 0) {
            this.status = StockStatus.PARTIAL;
        } else {
            this.status = StockStatus.MISSING;
        }
    }

    public String getName() { return name; }
    public double getRequiredQuantity() { return requiredQuantity; }
    public double getAvailableQuantity() { return availableQuantity; }
    public String getUnit() { return unit; }
    public StockStatus getStatus() { return status; }

    public double getDeficit() {
        return Math.max(0, requiredQuantity - availableQuantity);
    }
}
