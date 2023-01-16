package com.example.ohmygash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewReport extends AppCompatActivity {

    TextView Date,Users, General, Autoshop, Station, Admins, VerAuto, VerGas,ReqAuto,ReqGas,UnverAuto,UnverGas;
    Button Back, Delete;

    DatabaseReference repRef = FirebaseDatabase.getInstance().getReference("Reports");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        Date = findViewById(R.id.ReportDate);
        Users = findViewById(R.id.ReportUsers);
        General = findViewById(R.id.ReportGeneral);
        Autoshop = findViewById(R.id.ReportAutoshop);
        Station = findViewById(R.id.ReportStation);
        Admins = findViewById(R.id.ReportAdmins);
        VerAuto = findViewById(R.id.ReportVerAuto);
        VerGas = findViewById(R.id.ReportVerGas);
        ReqAuto = findViewById(R.id.ReportReqAuto);
        ReqGas = findViewById(R.id.ReportReqGas);
        UnverAuto = findViewById(R.id.ReportUnverAuto);
        UnverGas = findViewById(R.id.ReportUnverGas);

        Delete = findViewById(R.id.DeleteReport);
        Back = findViewById(R.id.BackReport);

        Intent ThisIntent = getIntent();
        String ReportId = ThisIntent.getStringExtra("ReportId");

        repRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot report = snapshot.child(ReportId);

                Date.setText(report.child("ReportDate").getValue().toString());
                Users.setText("Total Users: "+ report.child("Users").getValue().toString());
                General.setText("Total General Users: "+report.child("General").getValue().toString());
                Autoshop.setText("Total Autoshops: "+report.child("Autoshop").getValue().toString());
                Station.setText("Total Stations: "+report.child("Station").getValue().toString());
                Admins.setText("Total Admins: "+report.child("Admins").getValue().toString());
                VerAuto.setText("Verified Autoshops: "+report.child("VerAuto").getValue().toString());
                VerGas.setText("Verified Gas Stations: "+report.child("VerGas").getValue().toString());
                ReqAuto.setText("Autoshop Verify Requests: "+report.child("ReqAuto").getValue().toString());
                ReqGas.setText("Gas Station Verify Requests: "+report.child("ReqGas").getValue().toString());
                UnverAuto.setText("Unverified Autoshops: "+report.child("UnverAuto").getValue().toString());
                UnverGas.setText("Unverified Gas Stations: "+report.child("UnverGas").getValue().toString());

                Delete.setOnClickListener(view -> {
                    report.getRef().removeValue();
                    finish();
//                    Intent intent = new Intent(ViewReport.this,LocatePlace.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra("AccountTypeToLocate","Account Reports");
//                    startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Back.setOnClickListener(view->{
            finish();
        });

    }
}