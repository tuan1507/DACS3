package com.example.thucpham.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.thucpham.Fragment.ProductFragments.ChairFragment;
import com.example.thucpham.Fragment.ProductFragments.TableFragment;
import com.example.thucpham.Fragment.ProductFragments.MeatFragment;
import com.example.thucpham.Fragment.ProductFragments.TreeFragment;

public class ProductAdapter_tabLayout extends FragmentStateAdapter {
    public ProductAdapter_tabLayout(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new TreeFragment();
            case 1: return new TableFragment();
            case 2: return new MeatFragment();
            default: return new ChairFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
