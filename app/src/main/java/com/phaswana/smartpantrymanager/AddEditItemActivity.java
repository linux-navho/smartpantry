package com.phaswana.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddEditItemActivity extends AppCompatActivity {

    private View layoutSelectIngredient, layoutExpiryDate;
    private TextView tvSelectedIngredient, tvBoundUnit, tvQuantityValue;
    private TextView tvDisplayExpiry, tvExpiryRelativeText;
    private ChipGroup chipGroupPresets;
    private Button btnSave, btnDelete;
    private DatabaseHelper dbHelper;

    private long itemId = -1;
    private String selectedIngredientName = "";
    private String boundUnit = "pcs";
    private double currentQuantity = 1.0;
    private double currentStep = 1.0;
    private String selectedExpiryIso = "";

    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_add_edit_item);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        layoutSelectIngredient = findViewById(R.id.layoutSelectIngredient);
        layoutExpiryDate = findViewById(R.id.layoutExpiryDate);
        tvSelectedIngredient = findViewById(R.id.tvSelectedIngredient);
        tvBoundUnit = findViewById(R.id.tvBoundUnit);
        tvQuantityValue = findViewById(R.id.tvQuantityValue);
        tvDisplayExpiry = findViewById(R.id.tvDisplayExpiry);
        tvExpiryRelativeText = findViewById(R.id.tvExpiryRelativeText);
        chipGroupPresets = findViewById(R.id.chipGroupPresets);
        btnSave = findViewById(R.id.btnSaveItem);
        btnDelete = findViewById(R.id.btnDeleteItem);
        View btnMinus = findViewById(R.id.btnQtyMinus);
        View btnPlus = findViewById(R.id.btnQtyPlus);

        layoutSelectIngredient.setOnClickListener(v -> showIngredientPickerDialog());
        layoutExpiryDate.setOnClickListener(v -> showDatePicker());

        btnMinus.setOnClickListener(v -> {
            if (currentQuantity - currentStep >= currentStep) {
                currentQuantity -= currentStep;
                renderQuantity();
                highlightMatchingPreset();
            }
        });

        btnPlus.setOnClickListener(v -> {
            currentQuantity += currentStep;
            renderQuantity();
            highlightMatchingPreset();
        });

        String passedName = getIntent().getStringExtra("EXTRA_ITEM_NAME");
        double passedQty = getIntent().getDoubleExtra("EXTRA_ITEM_QUANTITY", -1.0);

        if (getIntent().hasExtra("EXTRA_ITEM_ID")) {
            itemId = getIntent().getLongExtra("EXTRA_ITEM_ID", -1);
            if (toolbar != null) toolbar.setTitle("Edit Ingredient");

            currentQuantity = passedQty > 0 ? passedQty : 1.0;
            selectedExpiryIso = getIntent().getStringExtra("EXTRA_ITEM_EXPIRY");

            applyIngredientSelection(passedName, false);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            if (toolbar != null) toolbar.setTitle("Add Ingredient");
            btnDelete.setVisibility(View.GONE);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            selectedExpiryIso = isoFormat.format(cal.getTime());

            if (passedName != null && !passedName.trim().isEmpty()) {
                // Pre-selected from Recipe Detail missing ingredient click
                if (passedQty > 0) {
                    currentQuantity = passedQty;
                    applyIngredientSelection(passedName, false);
                } else {
                    applyIngredientSelection(passedName, true);
                }
            } else {
                // Regular Add click from Pantry FAB
                List<String> list = IngredientCatalog.getNames();
                if (!list.isEmpty()) {
                    applyIngredientSelection(list.get(0), true);
                }
            }
        }

        renderExpiryDate();
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void showIngredientPickerDialog() {
        List<String> names = IngredientCatalog.getNames();
        String[] items = names.toArray(new String[0]);

        int currentIndex = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equalsIgnoreCase(selectedIngredientName)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Ingredient")
                .setSingleChoiceItems(items, currentIndex, (dialog, which) -> {
                    applyIngredientSelection(items[which], true);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyIngredientSelection(String name, boolean resetDefaultQty) {
        if (name == null || name.trim().isEmpty()) return;
        selectedIngredientName = name.trim();
        tvSelectedIngredient.setText(selectedIngredientName);

        IngredientCatalog.Spec spec = IngredientCatalog.getSpec(selectedIngredientName);
        boundUnit = spec.unit;
        currentStep = spec.step;
        tvBoundUnit.setText(boundUnit);

        if (resetDefaultQty) {
            currentQuantity = spec.defaultValue;
        }
        renderQuantity();
        populatePresetChips(spec);
    }

    private void populatePresetChips(IngredientCatalog.Spec spec) {
        chipGroupPresets.removeAllViews();
        for (double preset : spec.presets) {
            Chip chip = new Chip(this);
            String label = (preset % 1 == 0) ? String.valueOf((long) preset) : String.valueOf(preset);
            chip.setText(label + " " + spec.unit);
            chip.setTextSize(13);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            chip.setChipBackgroundColorResource(R.color.bg_surface);
            chip.setChipStrokeColorResource(R.color.border_subtle);
            chip.setChipStrokeWidth(1.0f);
            chip.setOnClickListener(v -> {
                currentQuantity = preset;
                renderQuantity();
                highlightMatchingPreset();
            });
            chipGroupPresets.addView(chip);
        }
        highlightMatchingPreset();
    }

    private void highlightMatchingPreset() {
        for (int i = 0; i < chipGroupPresets.getChildCount(); i++) {
            View v = chipGroupPresets.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                IngredientCatalog.Spec spec = IngredientCatalog.getSpec(selectedIngredientName);
                if (i < spec.presets.length && spec.presets[i] == currentQuantity) {
                    chip.setChipBackgroundColorResource(R.color.border_focus);
                    chip.setChipStrokeColorResource(R.color.brand_primary);
                    chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                } else {
                    chip.setChipBackgroundColorResource(R.color.bg_surface);
                    chip.setChipStrokeColorResource(R.color.border_subtle);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }
            }
        }
    }

    private void renderQuantity() {
        String formatted = (currentQuantity % 1 == 0)
                ? String.valueOf((long) currentQuantity)
                : String.valueOf(currentQuantity);
        tvQuantityValue.setText(formatted);
    }

    private void renderExpiryDate() {
        tvDisplayExpiry.setText(selectedExpiryIso);
        try {
            Date target = isoFormat.parse(selectedExpiryIso);
            Date today = isoFormat.parse(isoFormat.format(new Date()));
            if (target != null && today != null) {
                long diff = TimeUnit.MILLISECONDS.toDays(target.getTime() - today.getTime());
                if (diff < 0) {
                    tvExpiryRelativeText.setText("Expired " + Math.abs(diff) + (Math.abs(diff) == 1 ? " day ago" : " days ago"));
                    tvExpiryRelativeText.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
                } else if (diff == 0) {
                    tvExpiryRelativeText.setText("Expires today");
                    tvExpiryRelativeText.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
                } else if (diff <= 3) {
                    tvExpiryRelativeText.setText("Expires in " + diff + (diff == 1 ? " day" : " days"));
                    tvExpiryRelativeText.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
                } else {
                    tvExpiryRelativeText.setText("Expires in " + diff + " days");
                    tvExpiryRelativeText.setTextColor(ContextCompat.getColor(this, R.color.status_fresh_text));
                }
            }
        } catch (ParseException e) {
            tvExpiryRelativeText.setText("Date format valid");
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            if (selectedExpiryIso != null && !selectedExpiryIso.isEmpty()) {
                Date d = isoFormat.parse(selectedExpiryIso);
                if (d != null) cal.setTime(d);
            }
        } catch (ParseException ignored) {}

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedExpiryIso = isoFormat.format(selected.getTime());
                    renderExpiryDate();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void handleSave() {
        if (selectedIngredientName.isEmpty()) {
            Toast.makeText(this, "Please select an ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        PantryItem existing = dbHelper.getPantryItemByName(selectedIngredientName);

        if (itemId != -1 && existing != null && existing.getId() == itemId) {
            PantryItem item = new PantryItem(itemId, selectedIngredientName, currentQuantity, boundUnit, selectedExpiryIso);
            dbHelper.updatePantryItem(item);
            Toast.makeText(this, "Updated " + selectedIngredientName, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (existing != null) {
            showDuplicateItemDialog(existing);
            return;
        }

        if (itemId == -1) {
            PantryItem item = new PantryItem(selectedIngredientName, currentQuantity, boundUnit, selectedExpiryIso);
            dbHelper.insertPantryItem(item);
            Toast.makeText(this, "Added " + selectedIngredientName + " to pantry", Toast.LENGTH_SHORT).show();
        } else {
            PantryItem item = new PantryItem(itemId, selectedIngredientName, currentQuantity, boundUnit, selectedExpiryIso);
            dbHelper.updatePantryItem(item);
            Toast.makeText(this, "Updated " + selectedIngredientName, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showDuplicateItemDialog(PantryItem existing) {
        String existingQtyFormatted = (existing.getQuantity() % 1 == 0)
                ? String.valueOf((long) existing.getQuantity())
                : String.valueOf(existing.getQuantity());

        String newQtyFormatted = (currentQuantity % 1 == 0)
                ? String.valueOf((long) currentQuantity)
                : String.valueOf(currentQuantity);

        double mergedQty = existing.getQuantity() + currentQuantity;
        String mergedQtyFormatted = (mergedQty % 1 == 0)
                ? String.valueOf((long) mergedQty)
                : String.valueOf(mergedQty);

        String message = selectedIngredientName + " is already in your pantry ("
                + existingQtyFormatted + " " + existing.getUnit() + ").\n\n"
                + "Would you like to add " + newQtyFormatted + " " + boundUnit
                + " to current stock (Total: " + mergedQtyFormatted + " " + boundUnit + "), or replace it?";

        new AlertDialog.Builder(this)
                .setTitle("Ingredient Already Exists")
                .setMessage(message)
                .setPositiveButton("Add to Stock", (dialog, which) -> {
                    String earliestExpiry = existing.getExpiryDate().compareTo(selectedExpiryIso) <= 0
                            ? existing.getExpiryDate()
                            : selectedExpiryIso;

                    PantryItem updated = new PantryItem(existing.getId(), existing.getName(), mergedQty, existing.getUnit(), earliestExpiry);
                    dbHelper.updatePantryItem(updated);
                    Toast.makeText(this, "Merged stock: " + mergedQtyFormatted + " " + existing.getUnit(), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNeutralButton("Replace", (dialog, which) -> {
                    PantryItem replaced = new PantryItem(existing.getId(), selectedIngredientName, currentQuantity, boundUnit, selectedExpiryIso);
                    dbHelper.updatePantryItem(replaced);
                    Toast.makeText(this, "Replaced with " + newQtyFormatted + " " + boundUnit, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to remove " + selectedIngredientName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(itemId);
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
