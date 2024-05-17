package com.example.thucpham.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.thucpham.Fragment.Bill.CurrentOrderFragment;
import com.example.thucpham.Fragment.Bill.HistoryOrderFragment;

public class OderViewPagerAdapter extends FragmentStateAdapter {
    public OderViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new CurrentOrderFragment();
            default: return new HistoryOrderFragment();

        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
