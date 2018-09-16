package com.example.david.herosearch;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NeedHeroActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private String category = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG = "NeedHeroActivity";

    private String deadline_date;
    private String deadline_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_hero);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.call_categories, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_item);

        final EditText edit_date = findViewById(R.id.need_hero_date);
        final EditText edit_time = findViewById(R.id.need_hero_time);

        View.OnClickListener dialogFragmentDisplayListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == edit_date)
                {
                    DialogFragment date_dialog = new DatePickerFragment();
                    date_dialog.show(getSupportFragmentManager(), "dialog");
                }
                else if (v == edit_time)
                {
                    DialogFragment time_dialog = new TimePickerFragment();
                    time_dialog.show(getSupportFragmentManager(), "dialog");
                }
            }
        };

        edit_date.setOnClickListener(dialogFragmentDisplayListener);
        edit_time.setOnClickListener(dialogFragmentDisplayListener);

        Spinner need_hero_spinner = findViewById(R.id.call_category_spinner);
        need_hero_spinner.setAdapter(adapter);
        need_hero_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();

                final LinearLayout headline_layout = findViewById(R.id.need_hero_row_1_1);

                if (category.equals("Other"))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            headline_layout.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            headline_layout.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button submit_button = findViewById(R.id.call_submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

    }

    private void submit()
    {
        Calendar calendar = Calendar.getInstance(Locale.US);
        Date date = calendar.getTime();

        EditText need_hero_location_field = findViewById(R.id.need_hero_location_field);
        EditText need_hero_description = findViewById(R.id.need_hero_description);
        EditText need_hero_headline = findViewById(R.id.need_hero_headline);

        String location = need_hero_location_field.getText().toString();
        String description = need_hero_description.getText().toString();
        String headline = need_hero_headline.getText().toString();

        CheckBox terms_conditions = findViewById(R.id.terms_conditions_toggle);

        if (category.equals("Other") && headline.length() == 0)
        {
            Toast.makeText(this, "Headline is needed for calls of type \'Other\'.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (location.length() == 0)
        {
            Toast.makeText(this, "Location is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (description.length() == 0)
        {
            Toast.makeText(this, "Description is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!terms_conditions.isChecked())
        {
            Toast.makeText(this, "You must agree to enable location tracking.", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
        try{
            List<Address> addressList = geocoder.getFromLocationName(location, 1);

            HashMap<String, Object> data = new HashMap<>();
            data.put("category", category);
            data.put("caller_document_id", HomeActivity.documentID);
            if (category.equals("Other")) {
                data.put("headline", headline);
            }
            data.put("description", description);
            if (addressList.size() > 0) {
                data.put("location", addressList.get(0).getAddressLine(0));
            }
            else
            {
                Toast.makeText(this, "Enter a valid address.", Toast.LENGTH_SHORT).show();
                return;
            }

            data.put("firebase_instance_id", MainActivity.instanceID);
            data.put("call_posting_time", date.toString());
            data.put("call_deadline", deadline_date + " " + deadline_time);
            data.put("answering_heroes_ids", new ArrayList<>());
            data.put("answering_heroes_names", new ArrayList<>());

            data.put("caller_latitude", -1);
            data.put("caller_longitude", -1);
            data.put("responder_latitudes", new ArrayList<Double>());
            data.put("responder_longitudes", new ArrayList<Double>());

            db.collection("calls")
                    .add(data)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(NeedHeroActivity.this, "Successfully posted call with ID: " + task.getResult().getId(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            }
                            else
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(NeedHeroActivity.this, "Error writing to database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "Error writing to database");
                            }
                        }
                    });
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NeedHeroActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        deadline_time = String.format(Locale.US, "%d", hourOfDay) + ":" + String.format(Locale.US, "%d", minute);
        EditText time_field = findViewById(R.id.need_hero_time);
        time_field.setText(deadline_time);

    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        deadline_date = String.format(Locale.US, "%d", month) + "/" + String.format(Locale.US, "%d", day) + "/" + String.format(Locale.US, "%d", year);
        EditText date_field = findViewById(R.id.need_hero_date);
        date_field.setText(deadline_date);
    }
}
