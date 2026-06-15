package com.example.travelin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddStepActivity extends AppCompatActivity {
    public static final String EXTRA_TRIP_ID = "extra_trip_id";

    private EditText locationInput;
    private EditText descriptionInput;
    private EditText dateInput;
    private EditText timeInput;
    private long tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_step);

        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, 0);
        locationInput = findViewById(R.id.input_location_name);
        descriptionInput = findViewById(R.id.input_description);
        dateInput = findViewById(R.id.input_date);
        timeInput = findViewById(R.id.input_time);

        ImageButton backButton = findViewById(R.id.btn_add_step_back);
        backButton.setOnClickListener(v -> finish());

        dateInput.setFocusable(false);
        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setFocusable(false);
        timeInput.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.map_picker_card).setOnClickListener(v ->
                Toast.makeText(this, "Sélection de la position à venir", Toast.LENGTH_SHORT).show());
        findViewById(R.id.photo_upload_card).setOnClickListener(v ->
                Toast.makeText(this, "Ajout de photos à venir", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_save_step).setOnClickListener(v -> saveStep());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                dateInput.setText(String.format(Locale.FRANCE, "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) ->
                timeInput.setText(String.format(Locale.FRANCE, "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show();
    }

    private void saveStep() {
        String location = locationInput.getText().toString().trim();
        if (TextUtils.isEmpty(location)) {
            locationInput.setError("Lieu obligatoire");
            return;
        }

        if (tripId <= 0) {
            Toast.makeText(this, "Ce voyage doit être enregistré avant d'ajouter une étape", Toast.LENGTH_LONG).show();
            return;
        }

        long stepId = new TripDao(this).insertStep(
                tripId,
                location,
                descriptionInput.getText().toString().trim(),
                dateInput.getText().toString().trim(),
                timeInput.getText().toString().trim()
        );
        if (stepId == -1) {
            Toast.makeText(this, "Impossible d'enregistrer l'étape", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Étape enregistrée", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
