package com.example.ohmygash.manageinventoryfragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ManageVPAdapter extends FragmentStateAdapter {

    public ManageVPAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new ManageGasolineFragment();
            case 1:
                return new ManageProductsFragment();
            case 2:
                return new ManageServicesFragment();
        }
        return new ManageGasolineFragment();
    }


    @Override
    public int getItemCount() {
        return 3;
    }
}
