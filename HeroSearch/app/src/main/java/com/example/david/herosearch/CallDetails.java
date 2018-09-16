package com.example.david.herosearch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CallDetails extends AppCompatActivity {

    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG = "CallDetails";
    private String call_id;
    private String user_document_id;
    private String firebase_instance_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_details);

        Intent intent = getIntent();

        int mode = intent.getIntExtra("Mode", 0);

        call_id = intent.getStringExtra("call_id");
        String category = intent.getStringExtra("category");
        user_document_id = intent.getStringExtra("user_document_id");
        String location = intent.getStringExtra("location");
        String description = intent.getStringExtra("description");
        String heroes = intent.getStringExtra("answering_heroes");
        String headline = intent.getStringExtra("headline");

        TextView id_field = findViewById(R.id.call_id_field_2);
        TextView call_category_field = findViewById(R.id.call_category_field_2);
        TextView call_headline_field = findViewById(R.id.call_headline_field_2);
        TextView call_location_field = findViewById(R.id.call_location_field_2);
        final TextView caller_name_field = findViewById(R.id.caller_name_field_2);
        TextView caller_id_field = findViewById(R.id.caller_id_field_2);
        TextView heroes_field = findViewById(R.id.answering_heroes_field_2);
        TextView description_field = findViewById(R.id.description_field_2);

        id_field.setText(call_id);
        call_category_field.setText(category);
        if (headline.length() > 0) {
            call_headline_field.setText(headline);
            LinearLayout headline_layout = findViewById(R.id.headline_layout_2);
            headline_layout.setVisibility(View.VISIBLE);
        }
        call_location_field.setText(location);
        caller_id_field.setText(user_document_id);
        description_field.setText(description);
        heroes_field.setText(heroes);

        db.collection("users")
                .document(user_document_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String caller_name = task.getResult().get("name").toString();
                            caller_name_field.setText(caller_name);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CallDetails.this, "Error accessing database", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.d(TAG, "Error accessing database");
                        }
                    }
                });

        if (mode == 1)
        {
            Button answer_call_button = findViewById(R.id.respond_to_call);
            answer_call_button.setText("Send Message to Caller");
            answer_call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });

            Button tracking_button = findViewById(R.id.tracking_button);
            tracking_button.setVisibility(View.VISIBLE);

            tracking_button.setText("Track Caller Location");

            tracking_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("tracking", 1).putExtra("id", call_id));
                }
            });


        }

        else if (mode == 2)
        {
            Button answer_call_button = findViewById(R.id.respond_to_call);
            //answer_call_button.setText("Send Message to Hero");
            answer_call_button.setVisibility(View.GONE);
            answer_call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });

            Button tracking_button = findViewById(R.id.tracking_button);
            tracking_button.setVisibility(View.VISIBLE);

            tracking_button.setText("Track Hero Location(s)");

            tracking_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("tracking", 2).putExtra("id", call_id));
                }
            });

        }

        else
        {
            Button answer_call_button = findViewById(R.id.respond_to_call);
            answer_call_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateResponderStatus();
                }
            });
        }

    }

    private void updateResponderStatus()
    {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment fragment = ResponseDialog.newInstance(new responseDialogListener() {
            @Override
            public void onResponseDialogSubmit(final String message) {

                DialogFragment prev = (DialogFragment) getSupportFragmentManager().findFragmentByTag("dialog");

                if (prev != null) {
                    prev.dismiss();
                }

                db.collection("calls")
                        .document(call_id)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    ArrayList<String> hero_ids = (ArrayList<String>) task.getResult().get("answering_heroes_ids");
                                    ArrayList<String> hero_names = (ArrayList<String>) task.getResult().get("answering_heroes_names");

                                    ArrayList<Double> answering_heroes_latitudes = (ArrayList<Double>) task.getResult().get("responder_latitudes");
                                    ArrayList<Double> answering_heroes_longitudes = (ArrayList<Double>) task.getResult().get("responder_longitudes");

                                    hero_ids.add(HomeActivity.documentID);
                                    hero_names.add(HomeActivity.name);
                                    answering_heroes_latitudes.add(-1.0);
                                    answering_heroes_longitudes.add(-1.0);

                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put("answering_heroes_ids", hero_ids);
                                    data.put("answering_heroes_names", hero_names);
                                    data.put("responder_latitudes", answering_heroes_latitudes);
                                    data.put("responder_longitudes", answering_heroes_longitudes);

                                    firebase_instance_id = task.getResult().get("firebase_instance_id").toString();
                                    Log.d(TAG, "Firebase Instance ID: " + firebase_instance_id);

                                    db.collection("calls")
                                            .document(call_id)
                                            .set(data, SetOptions.merge())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(CallDetails.this, "Successfully wrote to database", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        notifyCaller(message);
                                                        startActivity(new Intent(getApplicationContext(), BeHeroActivity.class));
                                                    }
                                                    else
                                                    {
                                                        Log.d(TAG, "Error writing to documents");
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    Log.d(TAG, "Error retrieving documents.");
                                }
                            }
                        });
            }
        });

        fragment.show(ft, "dialog");


    }

    private void sendMessage() {

    }

    private Task<String> notifyCaller(String message) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("notified_caller_id", firebase_instance_id);
        data.put("call_id", call_id);
        data.put("hero_name", HomeActivity.name);
        data.put("message", message);

        return mFunctions
                .getHttpsCallable("notifyCaller")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
}
