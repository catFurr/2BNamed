package com.firstproj.a2bnamed.tabs;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.firstproj.a2bnamed.R;

public class mapCycleTab extends Fragment{

//    RecyclerView LockList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_cycle_tab, container, false);

        return rootView;
    }

    public void moveViewUp() {
//        LockList.getLayoutParams().height = 100;
//        LockList.setVisibility(View.VISIBLE);
//        LockList.bringToFront();

    }

}
