package com.example.ohmygash;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Map extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    FirebaseAuth FBAuth;

    //google map object
    GoogleMap mMap;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;

    //current and destination location objects
    Location myLocation = null;
    Location destinationLocation = null;
    LatLng start = null;
    LatLng end = null;

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;

    //polyline object
    private List<Polyline> polylines = null;

    Geocoder geocoder;
    List<Address> addresses;

    private EditText Searchbox;
    private Button Search, Directions, NearestAuto, NearestStat, Menu, MarkerButton, RouteType, Track, Buttons;
    private TextView DistanceToDestination;
    private String UserKeyToLocate, MarkerUserId;
    private AbstractRouting.TravelMode RouteTyping;
    private java.util.Map<Marker, DataSnapshot> markerMap;
    private java.util.Map<DataSnapshot, LatLng> locMap;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Intent ThisIntent = getIntent();
        UserKeyToLocate = ThisIntent.getStringExtra("UserKeyToLocate");

        Searchbox = findViewById(R.id.MapAdressSearchBar);
        Search = findViewById(R.id.MapSearchButton);
        Directions = findViewById(R.id.DirectionsButton);
        DistanceToDestination = findViewById(R.id.DistanceFromDestination);
        NearestAuto = findViewById(R.id.NearestAutoShopButton);
        NearestStat = findViewById(R.id.NearestStationButton);
        Menu = findViewById(R.id.NavigationButton);
        Track = findViewById(R.id.MapTrackButton);
        MarkerButton = findViewById(R.id.GoToMarker);
        RouteType = findViewById(R.id.RouteTyping);
        Buttons = findViewById(R.id.ButtonForButtons);

        FBAuth = FirebaseAuth.getInstance();
        RouteTyping = AbstractRouting.TravelMode.DRIVING;
        FirebaseUser user = FBAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(Map.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //request location permission.

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user : snapshot.getChildren()){
                    if ((user.child("accType").getValue().toString().matches("Station")||user.child("accType").getValue().toString().matches("Autoshop")) && user.child("status").getValue() == null) {
                        user.getRef().child("status").setValue("Unverified");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Search.setOnClickListener(view -> {
            geocoder = new Geocoder(this, Locale.getDefault());
            mMap.clear();
            try {
                addresses = geocoder.getFromLocationName(Searchbox.getText().toString(), 1);
                if (addresses.size() > 0) {
                    LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                    MarkerOptions endMarker = new MarkerOptions();
                    endMarker.position(LatiLongi);
                    endMarker.title("Search Result");
                    mMap.addMarker(endMarker);

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatiLongi, 16f);
                    mMap.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(Map.this, "Can't Locate Address", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Menu.setOnClickListener(view -> {
            Intent intent = new Intent(Map.this, Menu.class);
            startActivity(intent);
        });

        RouteType.setOnClickListener(view -> {
            if (RouteType.getText().toString().matches("Routing Type: DRIVING")) {
                RouteType.setText("Routing Type: WALKING");
                RouteTyping = AbstractRouting.TravelMode.WALKING;
            } else {
                RouteType.setText("Routing Type: DRIVING");
                RouteTyping = AbstractRouting.TravelMode.DRIVING;
            }
        });

        Buttons.setOnClickListener(view -> {
            if (Buttons.getText().toString().matches("Hide Buttons")) {
                Buttons.setText("Show Buttons");
                RouteType.setVisibility(View.GONE);
                Directions.setVisibility(View.GONE);
                Track.setVisibility(View.GONE);
                NearestAuto.setVisibility(View.GONE);
                NearestStat.setVisibility(View.GONE);
                MarkerButton.setVisibility(View.GONE);
            } else {
                Buttons.setText("Hide Buttons");
                RouteType.setVisibility(View.VISIBLE);
                Directions.setVisibility(View.VISIBLE);
                Track.setVisibility(View.VISIBLE);
                NearestAuto.setVisibility(View.VISIBLE);
                NearestStat.setVisibility(View.VISIBLE);
                MarkerButton.setVisibility(View.VISIBLE);
            }
        });

        Track.setOnClickListener(view -> {
            if (Track.getText().toString().matches("Follow Current Location")) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                    return;
                }

                LocationRequest mTrackRequest = new LocationRequest.Builder(PRIORITY_HIGH_ACCURACY,2000).build();

                mFusedLocationClient.requestLocationUpdates(mTrackRequest, mTrackCallback, Looper.myLooper());
                Track.setText("Stop Following Current Location");
            } else {
                mFusedLocationClient.removeLocationUpdates(mTrackCallback);
                Track.setText("Follow Current Location");
            }
        });

        Directions.setOnClickListener(view->{
            requestPermision();
            geocoder = new Geocoder(this,Locale.getDefault());
            mMap.clear();
            if (Directions.getText().toString().matches("Get Directions")) {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                mFusedLocationClient.removeLocationUpdates(mDirectionsCallback);
                try {
                    addresses = geocoder.getFromLocationName(Searchbox.getText().toString(),1);
                    if (addresses.size() > 0) {
                        Directions.setText("Stop Routing");
                        LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        end = LatiLongi;

                        mLocationRequest = new LocationRequest.Builder(PRIORITY_HIGH_ACCURACY,5000).build();

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                        } else {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mDirectionsCallback, Looper.myLooper());
                            mMap.setMyLocationEnabled(true);
                        }
                    } else {
                        Toast.makeText(Map.this, "Could not find location. Cannot make a route.", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Directions.setText("Get Directions");
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                mFusedLocationClient.removeLocationUpdates(mDirectionsCallback);
                DistanceToDestination.setText("");
            }
        });

        NearestAuto.setOnClickListener(view -> {
            mMap.clear();
            requestPermision();
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Toast.makeText(Map.this, "Searching for Nearest Autoshop", Toast.LENGTH_SHORT).show();
                    FindNearest(snapshot,"Autoshop");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        NearestStat.setOnClickListener(view -> {
            mMap.clear();
            requestPermision();
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Toast.makeText(Map.this, "Searching for Nearest Gas Station", Toast.LENGTH_SHORT).show();
                    FindNearest(snapshot,"Station");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        MarkerButton.setOnClickListener(view->{
            if (MarkerUserId!=null) {
                Intent intent = new Intent(view.getContext(), ViewPlace.class);
                intent.putExtra("UserId", MarkerUserId);
                view.getContext().startActivity(intent);
            }else{
                Toast.makeText(Map.this, "No Marker Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LatLng ConvertToDistanceFromMe(DataSnapshot user) throws IOException {
        if (user.child("placeLat").getValue() == null) {
            Geocoder geocoder = new Geocoder(Map.this, Locale.getDefault());
            List<Address> address1 = geocoder.getFromLocationName(user.child("placeAdd").getValue().toString(), 1);
            if (address1.size() > 0) {
                user.getRef().child("placeLat").setValue(address1.get(0).getLatitude());
                user.getRef().child("placeLng").setValue(address1.get(0).getLongitude());
                return new LatLng(address1.get(0).getLatitude(), address1.get(0).getLongitude());
            }
        } else {
            return new LatLng(Double.valueOf(user.child("placeLat").getValue().toString()), Double.valueOf(user.child("placeLng").getValue().toString()));
        }

        return new LatLng(0,0);
    };

    private void FindNearest(DataSnapshot snapshot, String accType){
        ArrayList<DataSnapshot> Users = new ArrayList<>();
        Iterable<DataSnapshot> snapshots = snapshot.getChildren();
        markerMap = new HashMap<Marker,DataSnapshot>();
        locMap = new HashMap<DataSnapshot,LatLng>();
        for (DataSnapshot user:snapshots){
            try {
                if (user.child("accType").getValue().toString().matches(accType)) {
                    String Address = user.child("placeAdd").getValue().toString();
                    locMap.put(user, ConvertToDistanceFromMe(user));
                    Users.add(user);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(Users, new Comparator<DataSnapshot>() {
            @Override
            public int compare(DataSnapshot t1, DataSnapshot t2) {
                LatLng pos1 = locMap.get(t1);
                LatLng pos2 = locMap.get(t2);
                int distance1 = 0;
                int distance2 = 0;
                if (pos1 != null && pos2 != null) {
                    Location place1 = new Location("");
                    place1.setLatitude(pos1.latitude);
                    place1.setLongitude(pos1.longitude);
                    distance1 = (int) myLocation.distanceTo(place1);
                    Location place2 = new Location("");
                    place2.setLatitude(pos2.latitude);
                    place2.setLongitude(pos2.longitude);
                    distance2 = (int) myLocation.distanceTo(place2);
                    return Integer.valueOf(distance1).compareTo(Integer.valueOf(distance2));
                }
                return 0;
                }
        });

        for (DataSnapshot user:Users){
            MarkerOptions endMarker = new MarkerOptions();
            LatLng pos = locMap.get(user);
            if (pos!=null){
            endMarker.position(pos);
            }
            endMarker.title(user.child("placeName").getValue().toString());
            Marker marker = mMap.addMarker(endMarker);
            markerMap.put(marker,user);
        }
        if (Users.size()>0) {
            DataSnapshot Nearest = Users.get(0);
            Searchbox.setText(Nearest.child("placeAdd").getValue().toString());

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    locMap.get(Users.get(0)), 16f);
            mMap.animateCamera(cameraUpdate);

            MarkerUserId = Nearest.getKey().toString();
        } else {
            Toast.makeText(this, "No Registered " + accType, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermision() {
        mLocationRequest = new LocationRequest.Builder(PRIORITY_HIGH_ACCURACY,500000).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                myLocation = location;

                LatLng ltlng=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());

                if (UserKeyToLocate != null) {
                    userRef.child(UserKeyToLocate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Searchbox.setText(snapshot.child("placeAdd").getValue().toString());
                            geocoder = new Geocoder(Map.this,Locale.getDefault());
                            mMap.clear();
                            try {
                                addresses = geocoder.getFromLocationName(Searchbox.getText().toString(),1);
                                if (addresses.size() > 0) {
                                    LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                                    MarkerOptions endMarker = new MarkerOptions();
                                    endMarker.position(LatiLongi);
                                    endMarker.title(snapshot.child("placeName").getValue().toString());
                                    Marker marker = mMap.addMarker(endMarker);
                                    markerMap = new HashMap<Marker, DataSnapshot>();
                                    markerMap.put(marker,snapshot);
                                    MarkerUserId = snapshot.getKey();
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                            LatiLongi, 16f);
                                    mMap.animateCamera(cameraUpdate);
                                } else {
                                    Toast.makeText(Map.this, "Can't Locate Address", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            ltlng, 16f);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        }
    };

    LocationCallback mDirectionsCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                geocoder = new Geocoder(Map.this,Locale.getDefault());
                mMap.clear();
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                Location location = locationList.get(locationList.size() - 1);
                myLocation = location;
                LatLng ltlngStart=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                try {
                    addresses = geocoder.getFromLocationName(Searchbox.getText().toString(),1);
                    LatLng ltlngEnd = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
                    end = ltlngEnd;
                    Findroutes(ltlngStart,ltlngEnd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    LocationCallback mTrackCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                myLocation = location;
                LatLng ltlngStart=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        ltlngStart, 16f);
                mMap.animateCamera(cameraUpdate);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    requestPermision();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }




    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        requestPermision();

        mMap.setOnMapClickListener(latLng -> {
            if (!Directions.getText().toString().matches("Stop Routing"))
            {
                end = latLng;
                mMap.clear();
                geocoder = new Geocoder(this, Locale.getDefault());
                String address = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    address = addresses.get(0).getAddressLine(0);

                    Searchbox.setText(address);

                    MarkerOptions endMarker = new MarkerOptions();
                    endMarker.position(latLng);
                    endMarker.title(address);
                    mMap.addMarker(endMarker);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                marker.showInfoWindow();
                DataSnapshot user = null;
                if (markerMap != null) {
                    user = markerMap.get(marker);
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        marker.getPosition(), 16.5f);
                mMap.animateCamera(cameraUpdate);
                if(user != null) {
                    MarkerUserId = user.getKey().toString();
                    Searchbox.setText(user.child("placeAdd").getValue().toString());
                }
                return true;
            }
        });
    }


    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(Map.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {
            Routing routing = new Routing.Builder()
                    .travelMode(RouteTyping)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCOzLIlwzHVPIQQ-OBioZuQfTkuMRx8ALY")
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, "Failed to Create a Route", Snackbar.LENGTH_SHORT);
        snackbar.show();
        Directions.setText("Get Directions");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mFusedLocationClient.removeLocationUpdates(mDirectionsCallback);
        DistanceToDestination.setText("");
    }

    @Override
    public void onRoutingStart() {

    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        int distance = 0;
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.primaryColor));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

                List<LatLng> latlngpoints = route.get(shortestRouteIndex).getPoints();

                for (int j=0;j<latlngpoints.size()-1;j++) {
                    Location locStart = new Location("");
                    locStart.setLatitude(latlngpoints.get(j).latitude);
                    locStart.setLongitude(latlngpoints.get(j).longitude);

                    Location locEnd = new Location("");
                    locEnd.setLatitude(latlngpoints.get(j + 1).latitude);
                    locEnd.setLongitude(latlngpoints.get(j + 1).longitude);

                    distance += locStart.distanceTo(locEnd);
                }
                DistanceToDestination.setText("Distance: "+distance+" meters");
            }
            else {
            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start,end);
    }

}