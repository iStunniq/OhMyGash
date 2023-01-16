package com.example.ohmygash;

import android.content.Intent;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceRecViewAdapter extends RecyclerView.Adapter<PlaceRecViewAdapter.ViewHolder>{
    @NonNull

    private ArrayList<DataSnapshot> Users = new ArrayList<>();
    private Map<DataSnapshot,Integer> locMap = new HashMap<DataSnapshot,Integer>();
    DatabaseReference gasRef = FirebaseDatabase.getInstance().getReference("Gasoline");
    private Intent placeIntent = new Intent();

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.placeitem,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataSnapshot currentUser = Users.get(position);
        holder.PlaceName.setText(currentUser.child("placeName").getValue().toString());
        holder.PlaceAdd.setText(currentUser.child("placeAdd").getValue().toString());

        List<Address> addresses = null;
        int Distance = locMap.get(Users.get(position));
        if (Distance!=0) {
            holder.Distance.setText("This place is " + Distance + " meters away");
        }else{
            holder.Distance.setText("Cannot Calculate Distance, Check Location Permissions");
        }
        if (currentUser.child("placePhoto").getValue() != null) {
            String Url = currentUser.child("placePhoto").getValue().toString();

            Picasso.with(holder.PlacePhoto.getContext()).load(Url).fit().into(holder.PlacePhoto);
        }

        if (currentUser.child("accType").getValue().toString().matches("Station")){
            holder.PlaceBrand.setText(currentUser.child("brand").getValue().toString());
            String key = currentUser.getKey();
            DatabaseReference currentGasRef = gasRef.child(key);
            currentGasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<DataSnapshot> gasses = new ArrayList<>();
                    Iterable<DataSnapshot> SnapGas = snapshot.getChildren();
                    for (DataSnapshot gas:SnapGas){
                        gasses.add(gas);
                    }
                    if (gasses.size() > 0) {
                        Collections.sort(gasses, new Comparator<DataSnapshot>() {
                            @Override
                            public int compare(DataSnapshot t1, DataSnapshot t2) {
                                return Float.valueOf(t1.child("Price").getValue().toString()).compareTo(Float.valueOf(t2.child("Price").getValue().toString()));
                            }
                        });
                        holder.PlaceGas.setText("Cheapest Gas: " + gasses.get(0).child("Name").getValue().toString() + "\n for ₱" + gasses.get(0).child("Price").getValue().toString() + "/Liter");
                    } else {
                        holder.PlaceGas.setText("No Registered Gas to Display");
                    }
                    if (placeIntent.getStringExtra("AccountTypeToLocate").matches("Requests") || placeIntent.getStringExtra("AccountTypeToLocate").matches("AllAccounts"))
                    {
                        holder.PlaceGas.setVisibility(View.VISIBLE);
                        holder.PlaceGas.setText("Verification Status: " + currentUser.child("status").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            if (placeIntent.getStringExtra("AccountTypeToLocate").matches("Requests") || placeIntent.getStringExtra("AccountTypeToLocate").matches("AllAccounts"))
            {
                holder.PlaceGas.setVisibility(View.VISIBLE);
                holder.PlaceGas.setText("Verification Status: " + currentUser.child("status").getValue().toString());
            }else {
                holder.PlaceGas.setVisibility(View.GONE);
                holder.PlaceBrand.setVisibility(View.GONE);
            }
        }
        holder.itemView.setOnClickListener(view -> {
            if (placeIntent.getStringExtra("AccountTypeToLocate").matches("Requests")){
                Intent intent = new Intent(view.getContext(), RequestPage.class);
                intent.putExtra("UserId", currentUser.getKey().toString());
                view.getContext().startActivity(intent);
            }else if (placeIntent.getStringExtra("AccountTypeToLocate").matches("AllAccounts")){
                Intent intent = new Intent(view.getContext(), RequestPage.class);
                intent.putExtra("UserId", currentUser.getKey().toString());
                intent.putExtra("AllAccounts", true);
                view.getContext().startActivity(intent);
            }else  {
                Intent intent = new Intent(view.getContext(), ViewPlace.class);
                intent.putExtra("UserId", currentUser.getKey().toString());
                view.getContext().startActivity(intent);
            }
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

    public void setPlaceIntent(Intent placeIntent) {
        this.placeIntent = placeIntent;
    }

    public void setLocMap(Map<DataSnapshot, Integer> locMap) {
        this.locMap = locMap;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView PlaceName,PlaceAdd,Distance,PlaceGas,PlaceBrand;
        ImageView PlacePhoto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            PlaceName = itemView.findViewById(R.id.RecViewPlaceName);
            PlaceAdd = itemView.findViewById(R.id.RecViewPlaceAdd);
            Distance = itemView.findViewById(R.id.RecViewDistance);
            PlacePhoto = itemView.findViewById(R.id.RecViewPhoto);
            PlaceGas = itemView.findViewById(R.id.RecViewPlaceGas);
            PlaceBrand = itemView.findViewById(R.id.recViewBrand);
        }

    }
}
