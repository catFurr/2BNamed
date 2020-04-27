package com.firstproj.a2bnamed.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class viewPagerCustom extends ViewPager {
    public viewPagerCustom(@NonNull Context context) {
        super(context);
    }

    public viewPagerCustom(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (this.getCurrentItem() == 1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getX() > getWidth()*0.05 && event.getX() < getWidth()*0.95) {
                    return false;
                }
            }
        }

        return super.onTouchEvent(event);
    }
}
