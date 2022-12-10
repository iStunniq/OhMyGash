package com.example.ohmygash.manageinventoryfragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ohmygash.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ManageProductsFragment extends Fragment {

    DatabaseReference prodRef = FirebaseDatabase.getInstance().getReference("Product");
    RecyclerView recyclerView;
    FirebaseAuth FBAuth;
    FirebaseUser FBUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_manage_products, container, false);
        FBAuth = FirebaseAuth.getInstance();
        FBUser = FBAuth.getCurrentUser();

        recyclerView = root.findViewById(R.id.ProductFragmentRecycler);


        Intent ThisIntent = getActivity().getIntent();
        String UserId = ThisIntent.getStringExtra("UserId");
        if (UserId != null) {
            prodRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<DataSnapshot> Items = new ArrayList<>();
                    Iterable<DataSnapshot> DBItems = snapshot.child(UserId).getChildren();

                    for (DataSnapshot item : DBItems) {
                        Items.add(item);
                    }

                    Collections.sort(Items, new Comparator<DataSnapshot>() {
                        @Override
                        public int compare(DataSnapshot t1, DataSnapshot t2) {
                            return t1.child("Name").getValue().toString().compareTo(t2.child("Name").getValue().toString());
                        }

                        ;
                    });

                    ManageItemViewAdapter adapter = new ManageItemViewAdapter();
                    adapter.setItems(Items);

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            prodRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<DataSnapshot> Items = new ArrayList<>();
                    Iterable<DataSnapshot> DBItems = snapshot.child(FBUser.getUid()).getChildren();

                    for (DataSnapshot item : DBItems) {
                        Items.add(item);
                    }

                    Collections.sort(Items, new Comparator<DataSnapshot>() {
                        @Override
                        public int compare(DataSnapshot t1, DataSnapshot t2) {
                            return t1.child("Name").getValue().toString().compareTo(t2.child("Name").getValue().toString());
                        }

                        ;
                    });

                    ManageItemViewAdapter adapter = new ManageItemViewAdapter();
                    adapter.setItems(Items);
                    adapter.setViewing(false);

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return root;
    }
}