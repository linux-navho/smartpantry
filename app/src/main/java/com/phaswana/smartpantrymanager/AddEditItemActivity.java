package com.phaswana.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditItemActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etExpiry;
    private Spinner spinnerUnit;
    private Button btnSave, btnDelete;
    private DatabaseHelper dbHelper;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        etName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etItemQuantity);
        etExpiry = findViewById(R.id.etItemExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        btnSave = findViewById(R.id.btnSaveItem);
        btnDelete = findViewById(R.id.btnDeleteItem);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);

        etExpiry.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra("EXTRA_ITEM_ID")) {
            itemId = getIntent().getLongExtra("EXTRA_ITEM_ID", -1);
            if (toolbar != null) toolbar.setTitle(R.string.title_edit_item);
            etName.setText(getIntent().getStringExtra("EXTRA_ITEM_NAME"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("EXTRA_ITEM_QUANTITY", 1.0)));
            etExpiry.setText(getIntent().getStringExtra("EXTRA_ITEM_EXPIRY"));

            String unit = getIntent().getStringExtra("EXTRA_ITEM_UNIT");
            if (unit != null) {
                int spinnerPos = adapter.getPosition(unit);
                if (spinnerPos >= 0) spinnerUnit.setSelection(spinnerPos);
            }

            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            if (toolbar != null) toolbar.setTitle(R.string.title_add_item);
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    etExpiry.setText(sdf.format(selected.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "pcs";

        if (name.isEmpty()) {
            etName.setError(getString(R.string.err_empty_name));
            etName.requestFocus();
            return;
        }

        if (qtyStr.isEmpty()) {
            etQuantity.setError(getString(R.string.err_empty_quantity));
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError(getString(R.string.err_invalid_quantity));
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.err_invalid_quantity));
            etQuantity.requestFocus();
            return;
        }

        if (expiry.isEmpty()) {
            etExpiry.setError(getString(R.string.err_empty_expiry));
            etExpiry.requestFocus();
            return;
        }

        if (itemId == -1) {
            PantryItem item = new PantryItem(name, quantity, unit, expiry);
            dbHelper.insertPantryItem(item);
            Toast.makeText(this, "Item added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            PantryItem item = new PantryItem(itemId, name, quantity, unit, expiry);
            dbHelper.updatePantryItem(item);
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to remove this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(itemId);
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
