package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewPlace extends AppCompatActivity {

    private Button Back,Locate;
    private TextView Name,Add;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

    String accType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        Intent ThisIntent = getIntent();
        String UserId = ThisIntent.getStringExtra("UserId");

        Back = findViewById(R.id.navigationButton2);
        Name = findViewById(R.id.textView4);
        Add = findViewById(R.id.textView5);
        Locate = findViewById(R.id.LocateAddress);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot user = snapshot.child(UserId);
                accType = user.child("accType").getValue().toString();
                Name.setText(user.child("placeName").getValue().toString());
                Add.setText(user.child("placeAdd").getValue().toString());

                Back.setOnClickListener(view->{
                    Intent intent = new Intent(ViewPlace.this,LocatePlace.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("AccountTypeToLocate",accType);
                    startActivity(intent);
                });

                Locate.setOnClickListener(view->{
                    Intent intent = new Intent(ViewPlace.this,Map.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("NameToLocate",Name.getText());
                    intent.putExtra("AddressToLocate",Add.getText());
                    startActivity(intent);
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}