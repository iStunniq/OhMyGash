package com.example.ohmygash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LocatePlace extends AppCompatActivity {

    private Button Menu;
    private TextView textView2,Loading;
    RecyclerView recyclerView;

    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;
    Location myLocation;
    LatLng myPosition;
    FirebaseAuth FBAuth;
    FirebaseUser FBUser;
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_place);

        recyclerView = findViewById(R.id.PlaceRecyclerView);
        Menu = findViewById(R.id.navigationButton);
        textView2 = findViewById(R.id.textView2);
        Loading = findViewById(R.id.LoadingText);
        Intent LocatePlaceIntent = this.getIntent();
        String Acctype = LocatePlaceIntent.getStringExtra("AccountTypeToLocate");
        textView2.setText("OH MY GASH\n"+Acctype);
        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        if (FBUser == null) {
            Intent intent = new Intent(LocatePlace.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        Menu.setOnClickListener(view -> {
            Intent intent = new Intent(LocatePlace.this,Menu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermision();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DataSnapshot> Users = new ArrayList<>();
                Iterable<DataSnapshot> DBUsers = snapshot.getChildren();

                for (DataSnapshot user : DBUsers) {
                    if (user.child("accType").getValue().toString().matches(Acctype)) {
                        Users.add(user);
                    }
                }

                Collections.sort(Users, new Comparator<DataSnapshot>() {
                            @Override
                            public int compare(DataSnapshot t1, DataSnapshot t2) {
                                int distance1 = 0;
                                int distance2 = 0;
                                try {
                                    distance1 = ConvertToDistanceFromMe(t1.child("placeAdd").getValue().toString());
                                    distance2 = ConvertToDistanceFromMe(t2.child("placeAdd").getValue().toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return Integer.valueOf(distance1).compareTo(Integer.valueOf(distance2));
                            }
                        }
                );

                PlaceRecViewAdapter adapter = new PlaceRecViewAdapter();
                adapter.setUser(Users);
                adapter.setMyLocation(myLocation);
                adapter.setGeocoder(new Geocoder(LocatePlace.this, Locale.getDefault()));

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(LocatePlace.this));
                Loading.setText("");

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                myLocation = location;
                myPosition = new LatLng(location.getLatitude(),location.getLongitude());
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        }
    };

    private int ConvertToDistanceFromMe(String address) throws IOException {
        Geocoder geocoder = new Geocoder(LocatePlace.this, Locale.getDefault());
        List<Address> address1 = geocoder.getFromLocationName(address,1);
        if (address1.size()>0){
            Location place1 = new Location("");
            place1.setLatitude(address1.get(0).getLatitude());
            place1.setLongitude(address1.get(0).getLongitude());
            return (int) myLocation.distanceTo(place1);
        }
        return 0;
    };

    private void requestPermision() {
        mLocationRequest = LocationRequest.create()
                .setInterval(500000000)
                .setFastestInterval(500000000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(100);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 23);
        }
        else if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 23);
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }
}