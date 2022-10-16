package com.example.ohmygash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Register extends AppCompatActivity {
        private Button Return, General, Workshop, Station;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Return = findViewById(R.id.StationToRegister);
        General = findViewById(R.id.GeneralRegister);
        Workshop = findViewById(R.id.WorkshopRegister);
        Station = findViewById(R.id.StationRegister);

        Return.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        General.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this,GeneralRegister.class);
            startActivity(intent);
        });
        Workshop.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this,WorkshopRegister.class);
            startActivity(intent);
        });
        Station.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this,StationRegister.class);
            startActivity(intent);
        });

    }
}