package com.example.weather.launch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.weather.GlobalActivity;
import com.example.weather.R;
import com.example.weather.activity.WeatherActivity;

public class FirstLaunch extends AppCompatActivity {

    TextView cityInput;
    TextView message;
    int init = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
        cityInput = (TextView) findViewById(R.id.city_input);
        message = (TextView) findViewById(R.id.intro_text);
        if (GlobalActivity.i == 0) {
            message.setText(getString(R.string.pick_city));
        }
        else {
            message.setText(getString(R.string.uh_oh));
        }
        Button goButton = (Button) findViewById(R.id.go_button);
        goButton.setText(getString(R.string.first_go_text));
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonFunction();
                if (init == 1) {
                    buttonFunction();
                    Intent intent = new Intent(FirstLaunch.this, WeatherActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Log.i("Loaded" , "Weather");
                    startActivity(intent);
                }
                Log.i("Changed" , "City");
            }
        });
    }

    public void buttonFunction() {
        GlobalActivity.cp.setCity(cityInput.getText().toString());
        init = 1;
    }
}