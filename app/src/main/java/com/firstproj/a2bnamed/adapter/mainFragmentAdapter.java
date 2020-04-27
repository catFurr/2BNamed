package com.firstproj.a2bnamed.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.firstproj.a2bnamed.tabs.dataUserTab;
import com.firstproj.a2bnamed.tabs.financeTab;
import com.firstproj.a2bnamed.tabs.mapCycleTab;

public class mainFragmentAdapter extends FragmentStatePagerAdapter {

    public mainFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fg;
        switch (position) {
            case 0:
                fg = new dataUserTab();
                break;
            case 1:
            default:
                fg = new mapCycleTab();
                break;
            case 2:
                fg = new financeTab();
                break;
        }
        return fg;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
