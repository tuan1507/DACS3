package com.example.thucpham.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.thucpham.Fragment.BottomNav.HomePageFragment;
import com.example.thucpham.Fragment.Bill.OrderFragment;
import com.example.thucpham.Fragment.BottomNav.PersonalFragment;
import com.example.thucpham.Fragment.BottomNav.VoucherFragment;

public class ViewPagerHomeAdapter extends FragmentStatePagerAdapter {
    public ViewPagerHomeAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return new VoucherFragment();
            case 2:
                return new OrderFragment();
            default:
                return new PersonalFragment();
            case 3:
                return new PersonalFragment();
            case 0:
                return new HomePageFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
