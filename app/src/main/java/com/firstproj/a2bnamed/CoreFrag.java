package com.firstproj.a2bnamed;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.firstproj.a2bnamed.adapter.customTabFrame;
import com.firstproj.a2bnamed.adapter.mainFragmentAdapter;
import com.firstproj.a2bnamed.adapter.recyclerViewAdapter;
import com.firstproj.a2bnamed.adapter.viewPagerCustom;
import com.firstproj.a2bnamed.adapter.viewmodel;
import com.firstproj.a2bnamed.dummy.DummyContent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.FieldValue;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class CoreFrag extends Fragment
        implements recyclerViewAdapter.OnListInteractionListener{

    public CoreFrag() {
        // Required empty public constructor
    }

    private static final String TAG = "coreFragTAG";

    private MapView mMapView;
    private View parentView;
    private viewmodel sharedViewModel;
    private PagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (parentView != null) return parentView;

        parentView = inflater.inflate(R.layout.activity_core_app, container, false);

        viewPagerCustom viewPager = parentView.findViewById(R.id.ca_view_pager);
        viewPager.setAdapter(adapter);
//        viewPager.setClipToPadding(false);
//        viewPager.setPadding(30, 0, 30, 0);
        viewPager.setCurrentItem(1);

        customTabFrame tabFrame = parentView.findViewById(R.id.ca_TabFrameView);

        RecyclerView lockList = parentView.findViewById(R.id.ca_lockListView);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        lockList.setLayoutManager(layoutManager);

        // specify an adapter
        RecyclerView.Adapter mAdapter = new recyclerViewAdapter(DummyContent.ITEMS, this);
        lockList.setAdapter(mAdapter);

        BottomSheetBehavior mBottomSheetList = BottomSheetBehavior.from(lockList);
        mBottomSheetList.setState(BottomSheetBehavior.STATE_HIDDEN);

        ViewGroup.LayoutParams params = lockList.getLayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        // An extra 40 below, but idk why
        int topFramePaddingPx = (int) Math.ceil((64 + 94 + 40) * displayMetrics.density);
        params.height = displayMetrics.heightPixels - topFramePaddingPx;
        lockList.setLayoutParams(params);

        tabFrame.setupTabWithUIComps(viewPager, mBottomSheetList);


        mMapView = parentView.findViewById(R.id.ca_mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(requireActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {
            // TODO Google Maps can get the user position.

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(13.008,74.796))      // Sets the center of the map to NITK
                    .zoom(17)                   // Sets the zoom
                    .bearing(-20)                // Sets the orientation of the camera to east
                    .tilt(35)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });

        sharedViewModel.userFirstName.observe(getViewLifecycleOwner(),
                s -> ((TextView)parentView.findViewById(R.id.ca_txtName))
                .setText(getString(R.string.ca_txt_name, s, sharedViewModel.userLastName.getValue())));

        sharedViewModel.userLastName.observe(getViewLifecycleOwner(),
                s -> ((TextView)parentView.findViewById(R.id.ca_txtName))
                .setText(getString(R.string.ca_txt_name, sharedViewModel.userFirstName.getValue(), s)));

        sharedViewModel.userRollNo.observe(getViewLifecycleOwner(),
                s -> ((TextView)parentView.findViewById(R.id.ca_txtRollNo)).setText(s));

        parentView.findViewById(R.id.ca_bttnIssue).setOnClickListener(view -> {
            NavDirections action =
                    CoreFragDirections.actionCoreFragToReportIssueFrag();
            Navigation.findNavController(view).navigate(action);
        });

        parentView.findViewById(R.id.ca_bttnEdit).setOnClickListener(view -> {
            NavDirections action =
                    CoreFragDirections.actionCoreFragToSettingsFrag();
            Navigation.findNavController(parentView).navigate(action);
        });

        return parentView;
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
        NavDirections action =
                CoreFragDirections.actionCoreFragToInTripScreenFrag();
        Navigation.findNavController(parentView).navigate(action);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sharedViewModel = new ViewModelProvider(requireActivity()).get(viewmodel.class);

        FirebaseDynamicLinks
                .getInstance()
                .getDynamicLink(requireActivity().getIntent())
                .addOnSuccessListener(requireActivity(), pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                    }


                    if (deepLink != null){
                        Log.d(TAG, "Deep Link: " + deepLink.toString());

                        String emailLink = deepLink.toString();
                        // Confirm the link is a sign-in with email link.
                        if (sharedViewModel.mAuth.isSignInWithEmailLink(emailLink)) {
                            // Retrieve the emailID from wherever you stored it
                            // Construct the email link credential from the current URL.

                            if (sharedViewModel.userEmailId.getValue() == null) {
                                Log.e(TAG, "Unexpectedly received bad email. (NULL)");
                                return;
                            }

                            AuthCredential credential =
                                    EmailAuthProvider.getCredentialWithLink(sharedViewModel.userEmailId.getValue(), emailLink);

                            // Link the credential to the current user.
                            sharedViewModel.user.linkWithCredential(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Successfully linked emailLink credential!");
                                            sharedViewModel.userDoc.update(getString(R.string.user_email_verified), true);
                                            sharedViewModel.userEmailVerified.setValue(true);
                                        } else {
                                            Log.e(TAG, "Error linking emailLink credential", task.getException());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(requireActivity(), e -> Log.w(TAG, "getDynamicLink:onFailure", e));

        sharedViewModel.userDoc.update(getString(R.string.user_last_timestamp), FieldValue.serverTimestamp());
        Long numTimesLoggedIn;
        numTimesLoggedIn = Objects.requireNonNull(sharedViewModel.getUserDoc().getValue()).getLong(getString(R.string.user_logged_in_times));
        sharedViewModel.userDoc.update(getString(R.string.user_logged_in_times),
                ++numTimesLoggedIn);

        adapter = new mainFragmentAdapter(requireActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }
}
