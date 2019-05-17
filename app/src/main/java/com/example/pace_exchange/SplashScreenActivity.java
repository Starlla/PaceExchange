package com.example.pace_exchange;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView peIcon;
    private static final int DELAY_TIME = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        peIcon = findViewById(R.id.splash_logo);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
            finish();
        }, DELAY_TIME);
    }
}
