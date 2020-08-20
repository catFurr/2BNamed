package com.firstproj.a2bnamed;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class settingsFrag extends Fragment {

    public settingsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = parentView.findViewById(R.id.sf_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Settings");
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        return parentView;
    }
}
