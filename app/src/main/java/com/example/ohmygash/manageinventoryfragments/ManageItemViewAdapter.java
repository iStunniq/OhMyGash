package com.example.ohmygash.manageinventoryfragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ohmygash.AddNewItem;
import com.example.ohmygash.Map;
import com.example.ohmygash.R;
import com.example.ohmygash.ViewPlace;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManageItemViewAdapter extends RecyclerView.Adapter<ManageItemViewAdapter.ViewHolder>{

    private ArrayList<DataSnapshot> Items = new ArrayList<>();
    private Boolean Viewing = true;

    @Override
    public ManageItemViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manageitem,parent,false);
        ManageItemViewAdapter.ViewHolder viewHolder = new ManageItemViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ManageItemViewAdapter.ViewHolder holder, int position) {
        holder.ItemName.setText(Items.get(position).child("Name").getValue().toString());
        holder.ItemDesc.setText(Items.get(position).child("Description").getValue().toString());
        if (Items.get(position).child("Photo").getValue() != null) {
            Picasso.with(holder.itemView.getContext()).load(Items.get(position).child("Photo").getValue().toString()).fit().into(holder.PlacePhoto);
        }

        DatabaseReference itemRef = Items.get(position).getRef();

        if (itemRef.getParent().getParent().getKey().toString().matches("Gasoline")) {
            holder.ItemPrice.setText("₱"+Items.get(position).child("Price").getValue().toString()+"/Liter");
        }else{
            if (Items.get(position).child("Price").getValue().toString().matches("0")){
                holder.ItemPrice.setText("Unset");
            }
            else {
                holder.ItemPrice.setText("₱" + Items.get(position).child("Price").getValue().toString());
            }
        };

        if (Viewing){
            holder.EditItem.setVisibility(View.GONE);
        }
        else {
            holder.EditItem.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), AddNewItem.class);
                intent.putExtra("ItemId", Items.get(position).getKey().toString());
                intent.putExtra("ItemUser", Items.get(position).getRef().getParent().getKey().toString());
                intent.putExtra("ItemType", Items.get(position).getRef().getParent().getParent().getKey().toString());
                view.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return Items.size();
    }

    public void setItems(@NonNull ArrayList<DataSnapshot> items) {
        this.Items = items;
        notifyDataSetChanged();
    }

    public void setViewing(Boolean viewing) {
        Viewing = viewing;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView ItemName,ItemDesc,ItemPrice;
        ImageView PlacePhoto;
        Button EditItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemName = itemView.findViewById(R.id.ManageItemName);
            ItemDesc = itemView.findViewById(R.id.ManageItemDesc);
            ItemPrice = itemView.findViewById(R.id.ManageItemCost);
            PlacePhoto = itemView.findViewById(R.id.ManageItemImage);
            EditItem = itemView.findViewById(R.id.ManageItemEdit);
        }
    }
}
