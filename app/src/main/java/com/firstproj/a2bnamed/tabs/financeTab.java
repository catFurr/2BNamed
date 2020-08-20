package com.firstproj.a2bnamed.tabs;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.firstproj.a2bnamed.R;


public class financeTab extends Fragment {

    private static final String TAG = "ft_Frag";

    private int balance = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_finance_tab, container, false);

        balance = 50;

        ((TextView) parentView.findViewById(R.id.ft_txtBalance)).setText(getString(R.string.ft_balance_view, balance, 0));

        parentView.findViewById(R.id.ft_bttnAddBalance).setOnClickListener(view -> {
            balance += 20;
            ((TextView) parentView.findViewById(R.id.ft_txtBalance)).setText(getString(R.string.ft_balance_view, balance, 0));
        });

        return parentView;
    }

}
