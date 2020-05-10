package com.firstproj.a2bnamed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.firstproj.a2bnamed.adapter.customTabFrame;
import com.firstproj.a2bnamed.adapter.mainFragmentAdapter;
import com.firstproj.a2bnamed.adapter.recyclerViewAdapter;
import com.firstproj.a2bnamed.adapter.viewPagerCustom;
import com.firstproj.a2bnamed.dummy.DummyContent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoreApp extends AppCompatActivity
        implements recyclerViewAdapter.OnListInteractionListener{

    private static final String TAG = "coreApp";

    public static final String USER_NAME_FIRST = "com.a2bnamed.usernamefirst";
    public static final String USER_NAME_LAST = "com.a2bnamed.usernamelast";
    public static final String USER_ROLL_NO = "com.a2bnamed.userrollno";
    public static final String USER_EMAIL = "com.a2bnamed.useremail";
    public static final String USER_EMAIL_CHECK = "com.a2bnamed.useremailcheck";

    public static FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    public static String userNameFirst;
    public static String userNameLast;
    public static String userRollNo;
    public static String userEmailID;
    public static boolean isEmailVerified;

    public viewPagerCustom viewPager;

    RecyclerView LockList;

    BottomSheetBehavior mBottomSheetList;

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_app);

        Intent starterIntent = getIntent();
        user = starterIntent.getParcelableExtra(MainActivity.UID_MESSAGE);
        userNameFirst = starterIntent.getStringExtra(USER_NAME_FIRST);
        userNameLast = starterIntent.getStringExtra(USER_NAME_LAST);
        userRollNo = starterIntent.getStringExtra(USER_ROLL_NO);
        userEmailID = starterIntent.getStringExtra(USER_EMAIL);
        isEmailVerified = starterIntent.getBooleanExtra(USER_EMAIL_CHECK, false);


        db = FirebaseFirestore.getInstance();
        userDocRef = db.collection(getString(R.string.users)).document(user.getUid());

        viewPager = findViewById(R.id.ca_view_pager);
        PagerAdapter adapter = new mainFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
//        viewPager.setClipToPadding(false);
//        viewPager.setPadding(30, 0, 30, 0);
        viewPager.setCurrentItem(1);

        customTabFrame tabFrame = findViewById(R.id.ca_TabFrameView);

        LockList = findViewById(R.id.ca_lockListView);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        LockList.setLayoutManager(layoutManager);

        // specify an adapter
        RecyclerView.Adapter mAdapter = new recyclerViewAdapter(DummyContent.ITEMS, this);
        LockList.setAdapter(mAdapter);

        mBottomSheetList = BottomSheetBehavior.from(LockList);
        mBottomSheetList.setState(BottomSheetBehavior.STATE_HIDDEN);

        tabFrame.setupTabWithUIComps(viewPager, mBottomSheetList);


        mMapView = findViewById(R.id.ca_mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;

            // TODO Google Maps can get the user position.
        });

    }


    @Override
    public void onStart() {
        super.onStart();

        ((TextView)findViewById(R.id.ca_txtName)).setText(getString(R.string.txtName, CoreApp.userNameFirst, CoreApp.userNameLast));
        ((TextView)findViewById(R.id.ca_txtRollNo)).setText(CoreApp.userRollNo);
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onClickedListItem() {
        Intent tripIntent = new Intent(this, inTripScreen.class);
        startActivity(tripIntent);
    }
}


/*
 TODO : List

 Reduce splash screen time by using a service for FireBase transactions.
 Use the Maps API to plot nests nearby
 Update last logged in value in FireBase
 Implement the BlueTooth Service
 Implement a method for payment
*/
