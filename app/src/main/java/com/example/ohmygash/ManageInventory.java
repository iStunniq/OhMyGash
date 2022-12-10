package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ohmygash.manageinventoryfragments.ManageVPAdapter;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageInventory extends AppCompatActivity {

    private Button Menu,NewItem;
    private TextView Title,Name,Address;
    private TabLayout ManageTabs;
    private ViewPager2 ManagePager;
    private ManageVPAdapter VPAdapter;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth FBAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);

        Menu = findViewById(R.id.ManageInventoryNavButton);
        NewItem = findViewById(R.id.AddNewItemButton);
        Title = findViewById(R.id.ManageTextViewTitle);
        Name = findViewById(R.id.ManageTextViewName);
        Address = findViewById(R.id.ManageTextViewAddress);
        ManageTabs = findViewById(R.id.ManageInventoryTabs);
        ManagePager = findViewById(R.id.ManageInventoryPager);
        FBAuth = FirebaseAuth.getInstance();
        FirebaseUser FBUser = FBAuth.getCurrentUser();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot user = snapshot.child(FBUser.getUid());
                Name.setText(user.child("placeName").getValue().toString());
                Address.setText(user.child("placeAdd").getValue().toString());
                if (!user.child("status").getValue().toString().matches("Verified")){
                    Toast.makeText(ManageInventory.this, "This Feature is Unavailable for unverified accounts", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Menu.getContext(),MyProfile.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                }
                if (user.child("accType").getValue().toString().matches("Autoshop")){
                    ManageTabs.getTabAt(0).view.setClickable(false);
                    ManageTabs.selectTab(ManageTabs.getTabAt(1));
                    ManagePager.setCurrentItem(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        VPAdapter = new ManageVPAdapter(fragmentManager, getLifecycle());
        ManagePager.setAdapter(VPAdapter);
        ManagePager.setOffscreenPageLimit(100);

        ManageTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ManagePager.setCurrentItem(tab.getPosition(),true);
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

        Menu.setOnClickListener(view -> {
            finish();
        });

        NewItem.setOnClickListener(view -> {
            Intent intent = new Intent(ManageInventory.this,AddNewItem.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

    }
}