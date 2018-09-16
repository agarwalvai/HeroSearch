package com.example.david.herosearch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BeHeroActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String TAG = "BeHeroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_be_hero);

        updateUI(0);

        TabLayout tabLayout = findViewById(R.id.be_hero_tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("All Calls"), 0, true);
        tabLayout.addTab(tabLayout.newTab().setText("Calls You Answered"), 1, false);
        tabLayout.addTab(tabLayout.newTab().setText("Your Calls"), 2, false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                {
                    updateUI(0);
                }
                else if (tab.getPosition() == 1)
                {
                    updateUI(1);
                }
                else if (tab.getPosition() == 2)
                {
                    updateUI(2);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void updateUI(int mode) {
        if (mode == 0) {
            db.collection("calls")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final LinearLayout container = findViewById(R.id.calls_linear_container);
                                container.removeAllViews();

                                final LayoutInflater li = getLayoutInflater();

                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    final View item = li.inflate(R.layout.call_item, container, false);

                                    final String call_id = documentSnapshot.getId();
                                    final String category = documentSnapshot.get("category").toString();
                                    final String user_document_id = documentSnapshot.get("caller_document_id").toString();

                                    String headline = "";

                                    final String location = documentSnapshot.get("location").toString();
                                    final String description = documentSnapshot.get("description").toString();
                                    ArrayList<String> answering_heroes = (ArrayList<String>) documentSnapshot.get("answering_heroes_names");

                                    //TextView id_field = item.findViewById(R.id.call_id_field);
                                    TextView category_field = item.findViewById(R.id.call_category_field);
                                    TextView location_field = item.findViewById(R.id.call_location_field);
                                    //TextView description_field = item.findViewById(R.id.description_field);
                                    TextView headline_field = item.findViewById(R.id.call_headline_field);
                                    TextView user_field = item.findViewById(R.id.caller_name_field);
                                    TextView heroes_field = item.findViewById(R.id.answering_heroes_field);

                                    TextView view_map = item.findViewById(R.id.view_in_map);
                                    view_map.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("address", location).putExtra("call_id", call_id));
                                        }
                                    });

                                    String heroes = "";
                                    int index = 0;

                                    for (String hero : answering_heroes) {
                                        heroes = heroes + hero;
                                        if (index != answering_heroes.size() - 1)
                                            heroes = heroes + ", ";
                                        index++;
                                    }

                                    final String final_headline = headline;
                                    final String final_heroes = heroes;
                                    if (category.equals("Other")) {
                                        headline = documentSnapshot.get("headline").toString();
                                        LinearLayout headline_layout = item.findViewById(R.id.headline_layout);
                                        headline_layout.setVisibility(View.VISIBLE);
                                        headline_field.setText(headline);
                                    }

                                    //id_field.setText(call_id);
                                    category_field.setText(category);
                                    location_field.setText(location);
                                    //description_field.setText(description);
                                    user_field.setText(user_document_id);
                                    heroes_field.setText(heroes);

                                    item.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getApplicationContext(), CallDetails.class);
                                            intent.putExtra("call_id", call_id);
                                            intent.putExtra("category", category);
                                            intent.putExtra("user_document_id", user_document_id);
                                            intent.putExtra("location", location);
                                            intent.putExtra("description", description);
                                            intent.putExtra("answering_heroes", final_heroes);
                                            intent.putExtra("headline", final_headline);

                                            startActivity(intent);
                                        }
                                    });

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            container.addView(item);
                                        }
                                    });

                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BeHeroActivity.this, "Error accessing database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "Error accessing database");
                            }
                        }
                    });
        }
        else if (mode == 1)
        {
            //Show answered calls
            db.collection("calls")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final LinearLayout container = findViewById(R.id.calls_linear_container);
                                container.removeAllViews();

                                final LayoutInflater li = getLayoutInflater();

                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    final View item = li.inflate(R.layout.call_item, container, false);

                                    final String call_id = documentSnapshot.getId();
                                    final String category = documentSnapshot.get("category").toString();
                                    final String user_document_id = documentSnapshot.get("caller_document_id").toString();

                                    String headline = "";

                                    final String location = documentSnapshot.get("location").toString();
                                    final String description = documentSnapshot.get("description").toString();
                                    ArrayList<String> answering_heroes = (ArrayList<String>) documentSnapshot.get("answering_heroes_names");
                                    ArrayList<String> answering_heroes_ids = (ArrayList<String>) documentSnapshot.get("answering_heroes_ids");

                                    //TextView id_field = item.findViewById(R.id.call_id_field);
                                    TextView category_field = item.findViewById(R.id.call_category_field);
                                    TextView location_field = item.findViewById(R.id.call_location_field);
                                    //TextView description_field = item.findViewById(R.id.description_field);
                                    TextView headline_field = item.findViewById(R.id.call_headline_field);
                                    TextView user_field = item.findViewById(R.id.caller_name_field);
                                    TextView heroes_field = item.findViewById(R.id.answering_heroes_field);

                                    TextView view_map = item.findViewById(R.id.view_in_map);
                                    view_map.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("address", location).putExtra("call_id", call_id));
                                        }
                                    });

                                    String heroes = "";
                                    boolean answered_by_user = false;
                                    int index = 0;
                                    for (String hero_id : answering_heroes_ids)
                                    {
                                        if (hero_id.equals(HomeActivity.documentID))
                                        {
                                            answered_by_user = true;
                                            break;
                                        }
                                        index++;
                                    }
                                    index = 0;
                                    for (String hero : answering_heroes) {
                                        heroes = heroes + hero;
                                        if (index != answering_heroes.size() - 1)
                                            heroes = heroes + ", ";
                                        index++;
                                    }

                                    final String final_headline = headline;
                                    final String final_heroes = heroes;
                                    if (category.equals("Other")) {
                                        headline = documentSnapshot.get("headline").toString();
                                        LinearLayout headline_layout = item.findViewById(R.id.headline_layout);
                                        headline_layout.setVisibility(View.VISIBLE);
                                        headline_field.setText(headline);
                                    }

                                    //id_field.setText(call_id);
                                    category_field.setText(category);
                                    location_field.setText(location);
                                    //description_field.setText(description);
                                    user_field.setText(user_document_id);
                                    heroes_field.setText(heroes);

                                    item.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getApplicationContext(), CallDetails.class);
                                            intent.putExtra("call_id", call_id);
                                            intent.putExtra("category", category);
                                            intent.putExtra("user_document_id", user_document_id);
                                            intent.putExtra("location", location);
                                            intent.putExtra("description", description);
                                            intent.putExtra("answering_heroes", final_heroes);
                                            intent.putExtra("headline", final_headline);

                                            intent.putExtra("Mode", 1);

                                            startActivity(intent);
                                        }
                                    });

                                    if (answered_by_user) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                container.addView(item);
                                            }
                                        });
                                    }

                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BeHeroActivity.this, "Error accessing database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "Error accessing database");
                            }
                        }
                    });
        }
        else if (mode == 2)
        {
            //Show your calls
            db.collection("calls")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final LinearLayout container = findViewById(R.id.calls_linear_container);
                                container.removeAllViews();

                                final LayoutInflater li = getLayoutInflater();

                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    final View item = li.inflate(R.layout.call_item, container, false);

                                    final String call_id = documentSnapshot.getId();
                                    final String category = documentSnapshot.get("category").toString();
                                    final String user_document_id = documentSnapshot.get("caller_document_id").toString();

                                    String headline = "";

                                    final String location = documentSnapshot.get("location").toString();
                                    final String description = documentSnapshot.get("description").toString();
                                    ArrayList<String> answering_heroes = (ArrayList<String>) documentSnapshot.get("answering_heroes_names");
                                    ArrayList<String> answering_heroes_ids = (ArrayList<String>) documentSnapshot.get("answering_heroes_ids");

                                    //TextView id_field = item.findViewById(R.id.call_id_field);
                                    TextView category_field = item.findViewById(R.id.call_category_field);
                                    TextView location_field = item.findViewById(R.id.call_location_field);
                                    //TextView description_field = item.findViewById(R.id.description_field);
                                    TextView headline_field = item.findViewById(R.id.call_headline_field);
                                    TextView user_field = item.findViewById(R.id.caller_name_field);
                                    TextView heroes_field = item.findViewById(R.id.answering_heroes_field);

                                    TextView view_map = item.findViewById(R.id.view_in_map);
                                    view_map.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(getApplicationContext(), MapsActivity.class).putExtra("address", location).putExtra("call_id", call_id));
                                        }
                                    });

                                    String heroes = "";
                                    boolean own_call = false;
                                    int index = 0;

                                    if (user_document_id.equals(HomeActivity.documentID))
                                    {
                                        own_call = true;
                                    }

                                    for (String hero : answering_heroes) {
                                        heroes = heroes + hero;
                                        if (index != answering_heroes.size() - 1)
                                            heroes = heroes + ", ";
                                        index++;
                                    }

                                    final String final_headline = headline;
                                    final String final_heroes = heroes;
                                    if (category.equals("Other")) {
                                        headline = documentSnapshot.get("headline").toString();
                                        LinearLayout headline_layout = item.findViewById(R.id.headline_layout);
                                        headline_layout.setVisibility(View.VISIBLE);
                                        headline_field.setText(headline);
                                    }

                                    //id_field.setText(call_id);
                                    category_field.setText(category);
                                    location_field.setText(location);
                                    //description_field.setText(description);
                                    user_field.setText(user_document_id);
                                    heroes_field.setText(heroes);

                                    item.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getApplicationContext(), CallDetails.class);
                                            intent.putExtra("call_id", call_id);
                                            intent.putExtra("category", category);
                                            intent.putExtra("user_document_id", user_document_id);
                                            intent.putExtra("location", location);
                                            intent.putExtra("description", description);
                                            intent.putExtra("answering_heroes", final_heroes);
                                            intent.putExtra("headline", final_headline);

                                            intent.putExtra("Mode", 2);

                                            startActivity(intent);
                                        }
                                    });

                                    if (own_call) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                container.addView(item);
                                            }
                                        });
                                    }

                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BeHeroActivity.this, "Error accessing database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "Error accessing database");
                            }
                        }
                    });
        }
    }
}
