package com.example.ohmygash;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

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
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocatePlace extends AppCompatActivity {

    private Button Menu,Filters;
    private TextView textView2,Loading;
    private FloatingActionButton Refresh;
    RecyclerView recyclerView;

    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;
    Location myLocation;
    LatLng myPosition;
    FirebaseAuth FBAuth;
    FirebaseUser FBUser;
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference gasRef = FirebaseDatabase.getInstance().getReference("Gasoline");
    DatabaseReference repRef = FirebaseDatabase.getInstance().getReference("Reports");
    Map<DataSnapshot,Integer> locMap;

    String FilterName, FilterType, FilterBrand,FilterStatus;
    int FilterRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_place);

        recyclerView = findViewById(R.id.PlaceRecyclerView);
        Menu = findViewById(R.id.navigationButton);
        Refresh = findViewById(R.id.RefreshButton);
        textView2 = findViewById(R.id.textView2);
        Loading = findViewById(R.id.LoadingText);
        Filters = findViewById(R.id.FiltersButton);
        Intent LocatePlaceIntent = this.getIntent();
        String Acctype = LocatePlaceIntent.getStringExtra("AccountTypeToLocate");

        FilterName = LocatePlaceIntent.getStringExtra("FilterName");
        FilterType = LocatePlaceIntent.getStringExtra("FilterType");
        FilterBrand = LocatePlaceIntent.getStringExtra("FilterBrand");
        FilterStatus = LocatePlaceIntent.getStringExtra("FilterStatus");
        FilterRadius = LocatePlaceIntent.getIntExtra("Radius",10000);

        textView2.setText("GAZZIR\n"+Acctype);
        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();
        if (FBUser == null) {
            Intent intent = new Intent(LocatePlace.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

//        if (Acctype.matches("Autoshop")) {
//            Filters.setVisibility(View.GONE);
//        }

        Filters.setOnClickListener(view ->{
            Intent intent = new Intent(LocatePlace.this,Filter.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("AccType",Acctype);
            startActivity(intent);
        });

        if (Acctype.matches("Requests")) {
            Filters.setText("All\nAccounts");
            Filters.setOnClickListener(view ->{
            Intent intent = new Intent(LocatePlace.this,LocatePlace.class);
            intent.putExtra("AccountTypeToLocate","AllAccounts");
            startActivity(intent);
            });
        } else if (Acctype.matches("Account Reports")) {
            Filters.setText("New\nReport");
            Filters.setOnClickListener(view ->{
                String Reportkey = repRef.push().getKey();
                DatabaseReference NewReport = repRef.child(Reportkey);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int Users = 0;
                        int General = 0;
                        int Autoshop = 0;
                        int Station = 0;
                        int Admins = 0;
                        int VerAuto  = 0;
                        int VerGas  = 0;
                        int ReqAuto = 0;
                        int ReqGas = 0;
                        int UnverAuto = 0;
                        int UnverGas = 0;
                        for (DataSnapshot user : snapshot.getChildren()){
                            Users+= 1;
                            if (user.child("accType").getValue().toString().matches("General")){
                                General+= 1;
                            } else if (user.child("accType").getValue().toString().matches("Autoshop")) {
                                Autoshop+= 1;
                                if (user.child("status").getValue().toString().matches("Verified")){
                                    VerAuto+= 1;
                                } else if (user.child("status").getValue().toString().matches("Requested")){
                                    ReqAuto+= 1;
                                } else if (user.child("status").getValue().toString().matches("Unverified")){
                                    UnverAuto+= 1;
                                }
                            } else if (user.child("accType").getValue().toString().matches("Station")) {
                                Station+= 1;
                                if (user.child("status").getValue().toString().matches("Verified")){
                                    VerGas+= 1;
                                } else if (user.child("status").getValue().toString().matches("Requested")){
                                    ReqGas+= 1;
                                } else if (user.child("status").getValue().toString().matches("Unverified")){
                                    UnverGas+= 1;
                                }
                            } else if (user.child("accType").getValue().toString().matches("Admin")) {
                                Admins+= 1;
                            }
                        }
                        NewReport.child("DateInMilli").setValue(Calendar.getInstance().getTimeInMillis());
                        NewReport.child("ReportDate").setValue(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime()));
                        NewReport.child("Users").setValue(Users);
                        NewReport.child("General").setValue(General);
                        NewReport.child("Autoshop").setValue(Autoshop);
                        NewReport.child("Station").setValue(Station);
                        NewReport.child("Admins").setValue(Admins);
                        NewReport.child("VerAuto").setValue(VerAuto);
                        NewReport.child("VerGas").setValue(VerGas);
                        NewReport.child("ReqAuto").setValue(ReqAuto);
                        NewReport.child("ReqGas").setValue(ReqGas);
                        NewReport.child("UnverAuto").setValue(UnverAuto);
                        NewReport.child("UnverGas").setValue(UnverGas);

                        Toast.makeText(LocatePlace.this, "Report Generated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            });
        }

        Menu.setOnClickListener(view -> {
            Intent intent = new Intent(LocatePlace.this,Menu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        Refresh.setOnClickListener(view -> {
            finish();
            startActivity(LocatePlaceIntent);
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermision();
        if (Acctype.matches("Account Reports")){
            repRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<DataSnapshot> Reports = new ArrayList<>();
                    Iterable<DataSnapshot> ReportSnapshots = snapshot.getChildren();

                    for (DataSnapshot report : ReportSnapshots){
                        if (report.child("DateInMilli").getValue() == null || report.child("Users").getValue() == null){
                            return;
                        }
                        Reports.add(report);
                    }

                    Collections.sort(Reports, new Comparator<DataSnapshot>() {
                            @Override
                            public int compare(DataSnapshot t1, DataSnapshot t2) {

                                long date1 = Long.parseLong(t1.child("DateInMilli").getValue().toString());
                                long date2 = Long.parseLong(t2.child("DateInMilli").getValue().toString());

                                if (date1 != 0 && date2 != 0) {
                                    return Long.compare(date2, date1);
                                }
                                return 0;
                            }
                        }
                    );

                    ReportViewAdapter adapter = new ReportViewAdapter();
                    adapter.setReports(Reports);

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(LocatePlace.this));
                    Loading.setText("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<DataSnapshot> Users = new ArrayList<>();
                    locMap = new HashMap<DataSnapshot, Integer>();
                    DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(FBUser.getUid());
                    favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            if (Acctype.matches("Favorites")) {
                                for (DataSnapshot favorite : snapshot1.getChildren()) {
                                    try {
                                        DataSnapshot user = snapshot.child(favorite.getValue().toString());
                                        if (user.child("brand").getValue() == null) {
                                            user.child("brand").getRef().setValue("Others");
                                        }
                                        int dist = ConvertToDistanceFromMe(user);
                                        if (UserFilters(user, dist)) {
                                            locMap.put(user, dist);
                                            Users.add(user);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if ((Acctype.matches("Requests"))) {
                                for (DataSnapshot user : snapshot.getChildren()) {
                                    if (user.child("brand").getValue() == null) {
                                        user.child("brand").getRef().setValue("Others");
                                    }
                                    if (user.child("status").getValue() != null) {
                                        try {
                                            int dist = ConvertToDistanceFromMe(user);
                                            if (user.child("status").getValue().toString().matches("Requested")) {
                                                if ((FilterName != null && !user.child("placeName").getValue().toString().toLowerCase(Locale.ROOT).contains(FilterName.toLowerCase(Locale.ROOT)))) {
                                                    continue;
                                                }
                                                if ((FilterBrand != null && !user.child("brand").getValue().toString().matches(FilterBrand))) {
                                                    continue;
                                                }
                                                if ((FilterStatus != null && !user.child("status").getValue().toString().matches(FilterStatus))) {
                                                    continue;
                                                }
                                                locMap.put(user, dist);
                                                Users.add(user);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            } else if ((Acctype.matches("AllAccounts"))) {
                                for (DataSnapshot user : snapshot.getChildren()) {
                                    if (user.child("brand").getValue() == null) {
                                        user.child("brand").getRef().setValue("Others");
                                    }
                                    if (user.child("status").getValue() != null) {
                                        try {
                                            int dist = ConvertToDistanceFromMe(user);
                                            if ((FilterName != null && !user.child("placeName").getValue().toString().toLowerCase(Locale.ROOT).contains(FilterName.toLowerCase(Locale.ROOT)))) {
                                                continue;
                                            }
                                            if ((FilterBrand != null && !user.child("brand").getValue().toString().matches(FilterBrand))) {
                                                continue;
                                            }
                                            if ((FilterStatus != null && !user.child("status").getValue().toString().matches(FilterStatus))) {
                                                continue;
                                            }
                                            locMap.put(user, dist);
                                            Users.add(user);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                Menu.setText("Back");
                                Menu.setOnClickListener(view -> {
                                    finish();
                                });
                            } else {
                                for (DataSnapshot user : snapshot.getChildren()) {
                                    if (user.child("brand").getValue() == null) {
                                        user.child("brand").getRef().setValue("Others");
                                    }
                                    if (user.child("accType").getValue().toString().matches(Acctype)) {
                                        try {
                                            int dist = ConvertToDistanceFromMe(user);
                                            if (UserFilters(user, dist)) {
                                                locMap.put(user, dist);
                                                Users.add(user);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if (FilterType != null && FilterType.matches("Price")) {
                                gasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (int i = Users.size() - 1; i >= 0; i--) {
                                            if (snapshot.child(Users.get(i).getKey()).getChildrenCount() == 0) {
                                                Users.remove(i);
                                            }
                                        }
                                        Collections.sort(Users, new Comparator<DataSnapshot>() {
                                                    @Override
                                                    public int compare(DataSnapshot t1, DataSnapshot t2) {
                                                        ArrayList<DataSnapshot> gasses1 = new ArrayList<>();
                                                        ArrayList<DataSnapshot> gasses2 = new ArrayList<>();

                                                        Iterable<DataSnapshot> SnapGas1 = snapshot.child(t1.getKey()).getChildren();
                                                        for (DataSnapshot gas : SnapGas1) {
                                                            gasses1.add(gas);
                                                        }
                                                        Iterable<DataSnapshot> SnapGas2 = snapshot.child(t2.getKey()).getChildren();
                                                        for (DataSnapshot gas : SnapGas2) {
                                                            gasses2.add(gas);
                                                        }
                                                        Collections.sort(gasses1, new Comparator<DataSnapshot>() {
                                                            @Override
                                                            public int compare(DataSnapshot t1, DataSnapshot t2) {
                                                                return Float.valueOf(t1.child("Price").getValue().toString()).compareTo(Float.valueOf(t2.child("Price").getValue().toString()));
                                                            }
                                                        });
                                                        Collections.sort(gasses2, new Comparator<DataSnapshot>() {
                                                            @Override
                                                            public int compare(DataSnapshot t1, DataSnapshot t2) {
                                                                return Float.valueOf(t1.child("Price").getValue().toString()).compareTo(Float.valueOf(t2.child("Price").getValue().toString()));
                                                            }
                                                        });
                                                        return Float.valueOf(gasses1.get(0).child("Price").getValue().toString()).compareTo(Float.valueOf(gasses2.get(0).child("Price").getValue().toString()));
                                                    }
                                                }
                                        );
                                        PlaceRecViewAdapter adapter = new PlaceRecViewAdapter();
                                        adapter.setUser(Users);
                                        adapter.setLocMap(locMap);
                                        adapter.setPlaceIntent(LocatePlaceIntent);

                                        recyclerView.setAdapter(adapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                Collections.sort(Users, new Comparator<DataSnapshot>() {
                                            @Override
                                            public int compare(DataSnapshot t1, DataSnapshot t2) {

                                                int distance1 = locMap.get(t1);
                                                int distance2 = locMap.get(t2);

                                                if (distance1 != 0 && distance2 != 0) {
                                                    return Integer.valueOf(distance1).compareTo(Integer.valueOf(distance2));
                                                }
                                                return 0;
                                            }
                                        }
                                );
                                PlaceRecViewAdapter adapter = new PlaceRecViewAdapter();
                                adapter.setUser(Users);
                                adapter.setLocMap(locMap);
                                adapter.setPlaceIntent(LocatePlaceIntent);

                                recyclerView.setAdapter(adapter);
                            }
                            recyclerView.setLayoutManager(new LinearLayoutManager(LocatePlace.this));
                            Loading.setText("");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private boolean UserFilters(DataSnapshot user, int dist){

        if ((FilterName != null && !user.child("placeName").getValue().toString().toLowerCase(Locale.ROOT).contains(FilterName.toLowerCase(Locale.ROOT)))) {
            return false;
        }
        if (dist > FilterRadius) {
            return false;
        }
        if ((FilterBrand != null && !user.child("brand").getValue().toString().matches(FilterBrand))){
            return false;
        }
        if (!user.child("status").getValue().toString().matches("Verified")){
            return false;
        }
        return true;
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

    private int ConvertToDistanceFromMe(DataSnapshot user) throws IOException {
        if (user.child("placeLat").getValue() == null) {
            Geocoder geocoder = new Geocoder(LocatePlace.this, Locale.getDefault());
            List<Address> address1 = geocoder.getFromLocationName(user.child("placeAdd").getValue().toString(), 1);
            if (address1.size() > 0) {
                Location place1 = new Location("");
                place1.setLatitude(address1.get(0).getLatitude());
                place1.setLongitude(address1.get(0).getLongitude());
                user.getRef().child("placeLat").setValue(address1.get(0).getLatitude());
                user.getRef().child("placeLng").setValue(address1.get(0).getLongitude());
                return (int) myLocation.distanceTo(place1);
            }
        }else{
            Location place1 = new Location("");
            place1.setLatitude(Double.valueOf(user.child("placeLat").getValue().toString()));
            place1.setLongitude(Double.valueOf(user.child("placeLng").getValue().toString()));
            if (myLocation != null) {
                return (int) myLocation.distanceTo(place1);
            }
        }
        return 0;
    };

    private void requestPermision() {
        mLocationRequest = new LocationRequest.Builder(PRIORITY_HIGH_ACCURACY,5000000).build();

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