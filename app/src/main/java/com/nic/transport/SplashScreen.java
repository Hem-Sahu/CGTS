package com.nic.transport;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    Handler handler;
    SharedPreferenceData sharedPreferenceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        handler = new Handler();

        sharedPreferenceData = new SharedPreferenceData(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              if (!sharedPreferenceData.getMobile("mobile").equals("")){
                    Intent intent = new Intent(SplashScreen.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(SplashScreen.this, GetOTP.class);
                    startActivity(intent);
                    finish();
                }
            }
        },2000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(()->
        {

        });
    }
}