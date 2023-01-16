package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestPage extends AppCompatActivity {

    TextView Name, Email, PlaceName, PlaceAdd, PlaceBrand, AccType, Status;
    Button Back, Approve, Deny;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_page);

        Status = findViewById(R.id.RequestVerificationStatus);
        Name = findViewById(R.id.RequestNameText);
        Email = findViewById(R.id.RequestEmailText);
        PlaceName = findViewById(R.id.RequestPlaceNameText);
        PlaceAdd = findViewById(R.id.RequestPlaceAddText);
        PlaceBrand = findViewById(R.id.RequestPlaceBrandText);
        AccType = findViewById(R.id.RequestAccTypeText);
        Approve = findViewById(R.id.ApproveRequest);
        Deny = findViewById(R.id.DenyRequest);
        Back = findViewById(R.id.BackRequest);

        String ApproveMessage = "Request Approved!";
        String DenyMessage = "Request Denied!";

        Intent ThisIntent = getIntent();
        String UserId = ThisIntent.getStringExtra("UserId");
        boolean AllAccounts = ThisIntent.getBooleanExtra("AllAccounts",false);

        if (AllAccounts) {
            Approve.setText("Verify Account");
            Deny.setText("Unverify Account");
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot user = snapshot.child(UserId);
                Status.setText("Verification Status: " + user.child("status").getValue().toString());
                Name.setText("Name: " + user.child("name").getValue().toString());
                Email.setText("Email: " + user.child("email").getValue().toString());
                PlaceName.setText("Place Name: " + user.child("placeName").getValue().toString());
                PlaceAdd.setText("Address: " + user.child("placeAdd").getValue().toString());
                PlaceBrand.setText("Brand: " + user.child("brand").getValue().toString());
                AccType.setText("Account Type: " + user.child("accType").getValue().toString());


                Approve.setOnClickListener(view -> {
                    if (AllAccounts) {
                        Toast.makeText(RequestPage.this, "User Status set to Verified", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RequestPage.this, "Request Approved!", Toast.LENGTH_SHORT).show();
                    }
                    user.getRef().child("status").setValue("Verified");
                });
                Deny.setOnClickListener(view -> {
                    if (AllAccounts) {
                        Toast.makeText(RequestPage.this, "User Status set to Unverified", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RequestPage.this, "Request Denied!", Toast.LENGTH_SHORT).show();
                    }
                    user.getRef().child("status").setValue("Unverified");
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