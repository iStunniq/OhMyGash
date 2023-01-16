package com.example.ohmygash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Register extends AppCompatActivity {
    private Button Return, General, Autoshop, Station;
    private FloatingActionButton Gen, Aut, Stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Return = findViewById(R.id.StationToRegister);
        General = findViewById(R.id.GeneralRegister);
        Autoshop = findViewById(R.id.AutoShopRegister);
        Station = findViewById(R.id.StationRegister);
        Gen = findViewById(R.id.GeneralFloating);
        Aut = findViewById(R.id.AutoshopFloating);
        Stat = findViewById(R.id.StationFloating);

        Return.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        General.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, GeneralRegister.class);
            startActivity(intent);
        });
        Autoshop.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, AutoshopRegister.class);
            startActivity(intent);
        });
        Station.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, StationRegister.class);
            startActivity(intent);
        });
        Gen.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, GeneralRegister.class);
            startActivity(intent);
        });
        Aut.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, AutoshopRegister.class);
            startActivity(intent);
        });
        Stat.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, StationRegister.class);
            startActivity(intent);
        });

    }
}