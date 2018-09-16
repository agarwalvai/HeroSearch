package com.example.david.herosearch;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    public static String name;
    public static String location;
    public static String documentID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG = "RegisterActivity";

    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        geocoder = new Geocoder(getApplicationContext(), Locale.US);

        Button register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        EditText name_field = findViewById(R.id.name_field);
        EditText location_field = findViewById(R.id.location_field);

        name = name_field.getText().toString();
        location = location_field.getText().toString();

        if (name.length() == 0)
        {
            Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.length() == 0)
        {
            Toast.makeText(this, "Location is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);

            if (addressList.size() == 0)
            {
                Toast.makeText(this, "Enter a valid address.", Toast.LENGTH_SHORT).show();
                return;
            }
            HashMap<String, Object> new_user = new HashMap<>();
            new_user.put("name", name);
            new_user.put("location", addressList.get(0).getAddressLine(0));
            new_user.put("email", MainActivity.email);
            new_user.put("firebase_id", MainActivity.firebase_id);

            db.collection("users")
                    .add(new_user)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful())
                            {
                                documentID = task.getResult().getId();
                                startActivity(new Intent(getApplicationContext(), RegistrationConfirmed.class));
                            }
                            else
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "Error writing to database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "Error writing to database");
                            }
                        }
                    });

        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
