package com.firstproj.a2bnamed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.firstproj.a2bnamed.adapter.mainFragmentAdapter;
import com.firstproj.a2bnamed.adapter.recyclerViewAdapter;
import com.firstproj.a2bnamed.adapter.viewPagerCustom;
import com.firstproj.a2bnamed.dummy.DummyContent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoreApp extends AppCompatActivity
        implements recyclerViewAdapter.OnListInteractionListener{

    private static String TAG = "coreApp";

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

    private Button bttnScan;
    RecyclerView LockList;

    MapView mMapView;
    private GoogleMap googleMap;

    @SuppressLint("ClickableViewAccessibility")
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

        viewPager = findViewById(R.id.view_pager);
        PagerAdapter adapter = new mainFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

//        coreAppViewModel mViewModel = ViewModelProviders.of(this).get(coreAppViewModel.class);

        bttnScan = findViewById(R.id.bttn_scan);
        bttnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutParams.height = 100;
                LockList.setLayoutParams(layoutParams);
            }
        });

        LockList = findViewById(R.id.lockListView);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        LockList.setLayoutManager(layoutManager);

        // specify an adapter
        RecyclerView.Adapter mAdapter = new recyclerViewAdapter(DummyContent.ITEMS, this);
        LockList.setAdapter(mAdapter);

        LockList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Sliding ListView.");
                int Y = (int) motionEvent.getRawY();
                getWindowManager().getDefaultDisplay().getSize(mdispSize);
                Y = mdispSize.y - Y;
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        _yDelta = Y - layoutParams.height;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.height = Y - _yDelta;
                        LockList.setLayoutParams(layoutParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_POINTER_UP:
                    default:
                        break;
                }
                return false;
            }
        });

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // Google Maps can get the user position.
            }
        });

    }

    private int _yDelta;
    ConstraintLayout.LayoutParams layoutParams;
    Point mdispSize = new Point();

    @Override
    public void onStart() {
        super.onStart();

        ((TextView)findViewById(R.id.txtName)).setText(getString(R.string.txtName, CoreApp.userNameFirst, CoreApp.userNameLast));
        ((TextView)findViewById(R.id.txtRollNo)).setText(CoreApp.userRollNo);

        layoutParams = (ConstraintLayout.LayoutParams) LockList.getLayoutParams();
        layoutParams.height = 0;
        LockList.setLayoutParams(layoutParams);
        LockList.bringToFront();
        findViewById(R.id.bttn_scan).setVisibility(View.VISIBLE);
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
