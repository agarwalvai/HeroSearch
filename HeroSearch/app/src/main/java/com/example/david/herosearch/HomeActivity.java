package com.example.david.herosearch;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.TabLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GoogleSignInClient mGoogleSignInClient;

    private String web_client_id = "271791558260-mte6ntsncjk9vur8jf9c77jnf5bpogpt.apps.googleusercontent.com";

    private Activity currentActivity;

    public static String name;
    public static String location;
    public static String documentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        int mode = getIntent().getIntExtra("Mode", -1);

        if (mode == 0)
        {
            name = RegisterActivity.name;
            location = RegisterActivity.location;
            documentID = RegisterActivity.documentID;
        }
        else if (mode == 1)
        {
            name = MainActivity.name;
            location = MainActivity.location;
            documentID = MainActivity.documentID;
        }


        CardView need_hero = findViewById(R.id.need_hero);
        CardView be_hero = findViewById(R.id.be_hero);

        need_hero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NeedHeroActivity.class));
            }
        });

        be_hero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BeHeroActivity.class));
            }
        });

        currentActivity = this;

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_client_id)
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        Button log_out_button = findViewById(R.id.log_out_button);
        log_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut()
    {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(currentActivity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Signed Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
