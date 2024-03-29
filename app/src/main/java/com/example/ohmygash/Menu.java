package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    TextView Name,Email, InventoryText, ReportsText;
    FloatingActionButton Autoshops,Stations,Map,Logout,Inventory,Profile,Favorites,Reports;
    FloatingActionButton Tutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Name = findViewById(R.id.MenuNameTextView);
        Email = findViewById(R.id.MenuEmailTextView);
        Autoshops = findViewById(R.id.GoToAutoShopsButton);
        Stations = findViewById(R.id.GoToStationsButton);
        Favorites = findViewById(R.id.GoToFavoritesButton);
        Profile = findViewById(R.id.GoToProfile);
        Inventory = findViewById(R.id.GoToInventory);
        InventoryText = findViewById(R.id.InventoryText);
        Logout = findViewById(R.id.LogoutButton);
        Map = findViewById(R.id.GoToMapButton);
        Tutorial = findViewById(R.id.MenuTutorialButton);
        Reports = findViewById(R.id.GoToReports);
        ReportsText = findViewById(R.id.ReportsText);

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

        Profile.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,MyProfile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Autoshops.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,LocatePlace.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccountTypeToLocate","Autoshop");
            startActivity(intent);
        });

        Stations.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,LocatePlace.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccountTypeToLocate","Station");
            startActivity(intent);
        });

        Favorites.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this,LocatePlace.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccountTypeToLocate","Favorites");
            startActivity(intent);
        });

        Logout.setOnClickListener(view->{
            FBAuth.signOut();
            Intent intent = new Intent(Menu.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        Tutorial.setOnClickListener(view->{
            Intent intent = new Intent(Menu.this, Tutorial1.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Tutorial",2);
            startActivity(intent);
        });
    }

    public void UpdateUI(FirebaseUser currentuser){
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   DataSnapshot user = snapshot.child(currentuser.getUid());
                   if (user.child("Tutorial2").getValue() == null){
                       Intent intent = new Intent(Menu.this, Tutorial1.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       intent.putExtra("Tutorial",2);
                       startActivity(intent);
                   }
                   Name.setText("Welcome back "+user.child("name").getValue().toString()+"!");
                   Email.setText(currentuser.getEmail());
                   String accType = user.child("accType").getValue().toString();
                   if (!accType.matches("General")){
                       Inventory.setVisibility(View.VISIBLE);
                       InventoryText.setVisibility(View.VISIBLE);
                       if (accType.matches("Admin")){
                           InventoryText.setText("Verifications");
                           Inventory.setImageDrawable(getDrawable(R.drawable.checkmark));
                           Inventory.setOnClickListener(view->{
                               Intent intent = new Intent(Menu.this,LocatePlace.class);
                               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               intent.putExtra("AccountTypeToLocate","Requests");
                               startActivity(intent);
                           });
                           Reports.setVisibility(View.VISIBLE);
                           ReportsText.setVisibility(View.VISIBLE);
                           Reports.setEnabled(true);
                           Reports.setOnClickListener(view->{
                               Intent intent = new Intent(Menu.this,LocatePlace.class);
                               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               intent.putExtra("AccountTypeToLocate","Account Reports");
                               startActivity(intent);
                           });
                       }else{
                           Inventory.setOnClickListener(view->{
                               Intent intent = new Intent(Menu.this,ManageInventory.class);
                               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               startActivity(intent);
                           });
                       }

                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           }
        );
    }
}