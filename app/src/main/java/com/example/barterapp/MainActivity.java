package com.example.barterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView userTest;
    Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userTest = findViewById(R.id.test_user);
        signOut = findViewById(R.id.sign_out_test);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        final FirebaseUser user = (FirebaseUser) bd.get("user");

        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            userTest.setText(email + "  " + uid);
        }


        signOut.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
    }
}
