package com.firstproj.a2bnamed.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.firstproj.a2bnamed.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class customTabFrame extends FrameLayout implements ViewPager.OnPageChangeListener {

    private ImageView infoTabBttn;
    private ImageView financeTabBttn;
    private ExtendedFloatingActionButton mapTabBttn;

    public customTabFrame(@NonNull Context context) {
        this(context, null);
    }

    public customTabFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public customTabFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_tabs_frame, this, true);

        infoTabBttn = findViewById(R.id.tf_bttnLeft);
        financeTabBttn = findViewById(R.id.tf_bttnRight);
        mapTabBttn = findViewById(R.id.tf_bttnScan);


    }


    public void setupTabWithUIComps(final viewPagerCustom viewPager, final BottomSheetBehavior bottomSheetList) {
        viewPager.addOnPageChangeListener(this);

        mapTabBttn.setOnClickListener(view -> {
            // Expand the recycler lock list view
            bottomSheetList.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Move to mapView Tab
            viewPager.setCurrentItem(1);
        });

        infoTabBttn.setOnClickListener(view -> {
            // Move to dataUser Tab
            viewPager.setCurrentItem(0);
        });

        financeTabBttn.setOnClickListener(view -> {
            // Move to finance Tab
            viewPager.setCurrentItem(2);
        });
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


}
