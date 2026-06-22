package com.akro.akrowats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("akrowats", MODE_PRIVATE);
            String serverUrl = prefs.getString("server_url", null);

            Intent intent;
            if (serverUrl != null) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, QrActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
