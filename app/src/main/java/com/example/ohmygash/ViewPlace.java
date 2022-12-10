package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.ohmygash.manageinventoryfragments.ManageVPAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewPlace extends AppCompatActivity {

    private Button Back,Locate,Favorite;
    private TextView Name,Add;
    private TabLayout ManageTabs;
    private ViewPager2 ManagePager;
    private ManageVPAdapter VPAdapter;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Favorites");

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
        ManageTabs = findViewById(R.id.ViewPlaceTabs);
        ManagePager = findViewById(R.id.ViewPlacePager);
        Favorite = findViewById(R.id.AddToFavorite);

        FirebaseAuth FBAuth = FirebaseAuth.getInstance();
        FirebaseUser FBUser = FBAuth.getCurrentUser();

        FragmentManager fragmentManager = getSupportFragmentManager();
        VPAdapter = new ManageVPAdapter(fragmentManager, getLifecycle());
        ManagePager.setOffscreenPageLimit(100);
        ManagePager.setAdapter(VPAdapter);

        ManageTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ManagePager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                ManagePager.setCurrentItem(tab.getPosition());
            }
        });

        ManagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ManageTabs.selectTab(ManageTabs.getTabAt(position));
            }
        });

        Back.setOnClickListener(view->{
//                    Intent intent = new Intent(ViewPlace.this,LocatePlace.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra("AccountTypeToLocate",accType);
//                    startActivity(intent);
            finish();
        });
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(FBUser.getUid()).child(UserId).getValue() != null){
                    Favorite.setText("Remove Favorite");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Favorite.setOnClickListener(view->{
            if (Favorite.getText().toString().matches("Add to Favorites")){
                Favorite.setText("Remove Favorite");
                favRef.child(FBUser.getUid()).child(UserId).setValue(UserId);
            } else {
                Favorite.setText("Add to Favorites");
                favRef.child(FBUser.getUid()).child(UserId).removeValue();
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot user = snapshot.child(UserId);
                accType = user.child("accType").getValue().toString();
                Name.setText(user.child("placeName").getValue().toString());
                Add.setText(user.child("placeAdd").getValue().toString());

                Locate.setOnClickListener(view->{
                    Intent intent = new Intent(ViewPlace.this,Map.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("UserKeyToLocate",user.getKey());
                    startActivity(intent);
                });

                if (accType.matches("Autoshop")){
                    ManageTabs.getTabAt(0).view.setClickable(false);
                    ManageTabs.selectTab(ManageTabs.getTabAt(1));
                    ManagePager.setCurrentItem(1);
                } else {
                    ManageTabs.selectTab(ManageTabs.getTabAt(0));
                    ManagePager.setCurrentItem(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}