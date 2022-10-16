package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    FirebaseAuth FBAuth;
    FirebaseUser FBUser;
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

    TextView Name,Email;
    Button Workshops,Stations,Map,Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Name = findViewById(R.id.MenuNameTextView);
        Email = findViewById(R.id.MenuEmailTextView);
        Workshops = findViewById(R.id.GoToWorkshopsButton);
        Stations = findViewById(R.id.GoToStationsButton);

        Logout = findViewById(R.id.LogoutButton);
        Map = findViewById(R.id.GoToMapButton);

        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        if (FBUser != null){
            UpdateUI(FBUser);
        }else{
            Intent intent = new Intent(Menu.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        Map.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,Map.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Workshops.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,LocatePlace.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccountTypeToLocate","Workshop");
            startActivity(intent);
        });

        Stations.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,LocatePlace.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccountTypeToLocate","Station");
            startActivity(intent);
        });

        Logout.setOnClickListener(view->{
            FBAuth.signOut();
            Intent intent = new Intent(Menu.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

    }

    public void UpdateUI(FirebaseUser currentuser){
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   DataSnapshot user = snapshot.child(currentuser.getUid());
                   Name.setText(user.child("name").getValue().toString());
                   Email.setText(currentuser.getEmail());
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           }
        );
    }
}