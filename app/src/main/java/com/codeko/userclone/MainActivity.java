package com.codeko.userclone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.ParseAnalytics;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void getStarted(View view) {
        Switch userTypeSwitch = findViewById(R.id.userTypeSwitch);
        Log.i("Switch value", String.valueOf(userTypeSwitch.isChecked()));
    }
}
