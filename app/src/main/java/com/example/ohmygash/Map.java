package com.example.ohmygash;

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

import java.io.IOException;
import java.util.ArrayList;
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
    boolean locationPermission = false;

    //polyline object
    private List<Polyline> polylines = null;

    Geocoder geocoder;
    List<Address> addresses;

    private EditText Searchbox;
    private Button Search,Directions,NearestWork,NearestStat,Menu;
    private TextView DistanceToDestination;

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Searchbox = findViewById(R.id.MapAdressSearchBar);
        Search = findViewById(R.id.MapSearchButton);
        Directions = findViewById(R.id.DirectionsButton);
        DistanceToDestination = findViewById(R.id.DistanceFromDestination);
        NearestWork = findViewById(R.id.NearestWorkshopButton);
        NearestStat = findViewById(R.id.NearestStationButton);
        Menu = findViewById(R.id.NavigationButton);
        FBAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FBAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(Map.this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //request location permission.

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Search.setOnClickListener(view->{
            geocoder = new Geocoder(this,Locale.getDefault());
            mMap.clear();
            try {
                addresses = geocoder.getFromLocationName(Searchbox.getText().toString(),1);
                LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());

                MarkerOptions endMarker = new MarkerOptions();
                endMarker.position(LatiLongi);
                endMarker.title("Search Result");
                mMap.addMarker(endMarker);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatiLongi, 16f);
                mMap.animateCamera(cameraUpdate);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Menu.setOnClickListener(view -> {
            Intent intent = new Intent(Map.this,Menu.class);
            startActivity(intent);
        });


        Directions.setOnClickListener(view->{
            geocoder = new Geocoder(this,Locale.getDefault());
            mMap.clear();
            if (Directions.getText().toString().matches("Get Directions")) {
                Directions.setText("Stop Routing");
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                mFusedLocationClient.removeLocationUpdates(mDirectionsCallback);
                try {
                    addresses = geocoder.getFromLocationName(Searchbox.getText().toString(),1);
                    LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
                    end = LatiLongi;

                    mLocationRequest = LocationRequest.create()
                            .setInterval(5000)
                            .setFastestInterval(5000)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setMaxWaitTime(100);

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                    }
                    else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                    }
                    else {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mDirectionsCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
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

        NearestWork.setOnClickListener(view -> {
            mMap.clear();
            requestPermision();
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FindNearest(snapshot,"Workshop");
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
                    FindNearest(snapshot,"Station");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });
    }

    private void FindNearest(DataSnapshot snapshot, String accType){
        Iterable<DataSnapshot> users = snapshot.getChildren();
        DataSnapshot Nearest = null;
        for (DataSnapshot user : users){
            if (user.child("accType").getValue().toString().matches(accType)){
                if (Nearest == null){
                    Nearest = user;
                } else {
                    geocoder = new Geocoder(Map.this,Locale.getDefault());
                    try {
                        List<Address> addresses1 = geocoder.getFromLocationName(user.child("placeAdd").getValue().toString(),1);
                        List<Address> addresses2 = geocoder.getFromLocationName(Nearest.child("placeAdd").getValue().toString(),1);

                        Location locEnd1 = new Location("");
                        locEnd1.setLatitude(addresses1.get(0).getLatitude());
                        locEnd1.setLongitude(addresses1.get(0).getLongitude());

                        Location locEnd2 = new Location("");
                        locEnd2.setLatitude(addresses2.get(0).getLatitude());
                        locEnd2.setLongitude(addresses2.get(0).getLongitude());

                        int val1 = (int) myLocation.distanceTo(locEnd1);
                        int val2 = (int) myLocation.distanceTo(locEnd2);

                        if (val1 < val2) {
                            Nearest = user;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }
        }
        geocoder = new Geocoder(Map.this,Locale.getDefault());
        try {
            Searchbox.setText(Nearest.child("placeAdd").getValue().toString());
            addresses = geocoder.getFromLocationName(Nearest.child("placeAdd").getValue().toString(),1);
            LatLng LatiLongi = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());

            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(LatiLongi);
            endMarker.title(Nearest.child("placeName").getValue().toString());
            mMap.addMarker(endMarker);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatiLongi, 16f);
            mMap.animateCamera(cameraUpdate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermision() {
        mLocationRequest = LocationRequest.create()
                .setInterval(500000000)
                .setFastestInterval(500000000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(100);

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

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        ltlng, 16f);
                mMap.animateCamera(cameraUpdate);
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
                start = ltlngStart;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        start, 18f);
                mMap.animateCamera(cameraUpdate);
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

            end=latLng;

            mMap.clear();

            geocoder = new Geocoder(this,Locale.getDefault());

            String address = null;

            try {
                addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                address = addresses.get(0).getAddressLine(0);

                Searchbox.setText(address);

                MarkerOptions endMarker = new MarkerOptions();
                endMarker.position(latLng);
                endMarker.title(address);
                mMap.addMarker(endMarker);

            } catch (IOException e) {
                e.printStackTrace();
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
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCOzLIlwzHVPIQQ-OBioZuQfTkuMRx8ALY")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {

    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
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