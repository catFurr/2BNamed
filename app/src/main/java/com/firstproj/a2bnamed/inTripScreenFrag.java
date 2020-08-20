package com.firstproj.a2bnamed;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class inTripScreenFrag extends Fragment {

    public inTripScreenFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.activity_trip_screen, container, false);

        Toolbar toolbar = parentView.findViewById(R.id.ts_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("In Trip Screen");
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        return parentView;
    }

}
