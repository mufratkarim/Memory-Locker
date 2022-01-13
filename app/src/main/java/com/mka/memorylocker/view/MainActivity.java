package com.mka.memorylocker.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.mka.memorylocker.R;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private Button buttonStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        buttonStarted = findViewById(R.id.buttonStarted);

        buttonStarted.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}