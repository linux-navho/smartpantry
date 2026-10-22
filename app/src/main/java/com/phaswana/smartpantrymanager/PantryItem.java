package com.phaswana.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PantryItem {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // Format: YYYY-MM-DD

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(-1, name, quantity, unit, expiryDate);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    /**
     * Calculates days until expiry relative to the current device date.
     * Negative values indicate expired items.
     */
    public long getDaysUntilExpiry() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setLenient(false);
        try {
            Date expiry = sdf.parse(this.expiryDate);
            Date today = sdf.parse(sdf.format(new Date()));
            if (expiry != null && today != null) {
                long diff = expiry.getTime() - today.getTime();
                return diff / (1000 * 60 * 60 * 24);
            }
        } catch (ParseException ignored) {}
        return 0;
    }
}
