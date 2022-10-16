package com.example.ohmygash;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceRecViewAdapter extends RecyclerView.Adapter<PlaceRecViewAdapter.ViewHolder>{
    @NonNull

    private ArrayList<DataSnapshot> Users = new ArrayList<>();
    private Location myLocation;
    private Geocoder geocoder;

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.placeitem,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.PlaceName.setText(Users.get(position).child("placeName").getValue().toString());
        holder.PlaceAdd.setText(Users.get(position).child("placeAdd").getValue().toString());

        List<Address> addresses = null;
        int Distance = 0;
        try {
            addresses = geocoder.getFromLocationName(Users.get(position).child("placeAdd").getValue().toString(),1);
            if (addresses.size() > 0) {

                Location place = new Location("");
                place.setLatitude(addresses.get(0).getLatitude());
                place.setLongitude(addresses.get(0).getLongitude());

                Distance = (int) myLocation.distanceTo(place);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
            holder.Distance.setText("This place is " + Distance + " meters away");

        holder.PlacePhoto.setOnClickListener(view->{
            Intent intent = new Intent(view.getContext(),ViewPlace.class);
            intent.putExtra("UserId",Users.get(position).getKey().toString());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Users.size();
    }

    public void setUser(@NonNull ArrayList<DataSnapshot> user) {
        this.Users = user;
        notifyDataSetChanged();
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView PlaceName,PlaceAdd,Distance;
        ImageView PlacePhoto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            PlaceName = itemView.findViewById(R.id.RecViewPlaceName);
            PlaceAdd = itemView.findViewById(R.id.RecViewPlaceAdd);
            Distance = itemView.findViewById(R.id.RecViewDistance);
            PlacePhoto = itemView.findViewById(R.id.RecViewPhoto);
        }

    }
}
