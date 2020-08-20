package com.firstproj.a2bnamed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.firstproj.a2bnamed.adapter.viewmodel;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class accountCreationFrag extends Fragment {

    public accountCreationFrag() {
        // Required empty public constructor
    }

    private static String TAG = "AccountCreationActivity";

    private View parentView;
    private viewmodel sharedViewModel;

    private Button mNextButton;
    private EditText mRollNo;
    private EditText mNameFirst;
    private EditText mNameLast;
    private EditText mEmailAddr;
    private ViewGroup mNameFirstLastView;
    private ViewGroup mRollNoView;
    private ViewGroup mEmailAddrView;

    private static final int STATE_NAME = 0;
    private static final int STATE_ROLLNO = 2;
    private static final int STATE_EMAIL = 3;
    private static final int STATE_EMAIL_SENT = 4;
    private int uiState;

    private String emailId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.activity_account_creation, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(viewmodel.class);

        mRollNo = parentView.findViewById(R.id.userRollNo);
        mNameFirst = parentView.findViewById(R.id.firstName);
        mNameLast = parentView.findViewById(R.id.lastName);
        mEmailAddr = parentView.findViewById(R.id.emailAddr);

        mNameFirstLastView = parentView.findViewById(R.id.firstlastnameview);
        mRollNoView = parentView.findViewById(R.id.rollnoview);
        mEmailAddrView = parentView.findViewById(R.id.emailAddrView);

        mNextButton = parentView.findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(view -> {
            switch(uiState) {
                case STATE_NAME:
                    uiState = STATE_ROLLNO;
                    break;
                case STATE_ROLLNO:
                    uiState = STATE_EMAIL;
                    break;
                case STATE_EMAIL:
                    emailLinkAuth();
                    break;
            }
            updateUI();
        });

        uiState = STATE_NAME;
        updateUI();

        return parentView;
    }


    private void updateUI() {
        switch (uiState) {
            case STATE_NAME:
                enableViews(mNameFirst, mNameLast);
                disableViews(mRollNo, mEmailAddr);
                mNameFirstLastView.setVisibility(View.VISIBLE);
                mRollNoView.setVisibility(View.GONE);
                mEmailAddrView.setVisibility(View.GONE);
                break;
            case STATE_ROLLNO:
                enableViews(mRollNo);
                disableViews(mNameFirst, mNameLast);
                mNameFirstLastView.setVisibility(View.GONE);
                mRollNoView.setVisibility(View.VISIBLE);
                break;
            case STATE_EMAIL:
                enableViews(mEmailAddr);
                disableViews(mRollNo);
                mRollNoView.setVisibility(View.GONE);
                mEmailAddrView.setVisibility(View.VISIBLE);
                break;
            case STATE_EMAIL_SENT:
                disableViews(mEmailAddr, mNextButton);
                break;
        }
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private void emailLinkAuth() {
        emailId = mEmailAddr.getText().toString();
//        if (!emailId.endsWith("@nitk.edu.in"))
//        {
//            mEmailAddr.setError("NITK Email only");
//            return;
//        }

        String url = "http://named-2empty.firebaseapp.com/verify?uid=" + sharedViewModel.getUserUid();
        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl(url)
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setIOSBundleId("com.example.ios")
                        .setAndroidPackageName(
                                "com.firstproj.a2bnamed",
                                false, /* installIfNotAvailable */
                                null    /* minimumVersion */)
                        .build();

        Log.d(TAG, "Email ActionCode Built Successfully");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendSignInLinkToEmail(emailId, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent.");
                        makeAccount();
                    } else {
                        Log.d(TAG, "Task failed: " + task.getException());
                    }
                });

        uiState = STATE_EMAIL_SENT;
        updateUI();
    }

    private void makeAccount() {
        sharedViewModel.db = FirebaseFirestore.getInstance();
        sharedViewModel.userDoc = sharedViewModel.db.collection(getString(R.string.fb_users)).document(sharedViewModel.getUserUid());

        String nameFirst = mNameFirst.getText().toString();
        String nameLast = mNameLast.getText().toString();
        String rollNo = mRollNo.getText().toString();

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put(getString(R.string.user_first_name), nameFirst);
        userDetails.put(getString(R.string.user_last_name), nameLast);
        userDetails.put(getString(R.string.user_roll_no), rollNo);
        userDetails.put(getString(R.string.user_email_verified), false);
        userDetails.put(getString(R.string.user_email_id), emailId);
        userDetails.put(getString(R.string.user_logged_in_times), 0);
        userDetails.put(getString(R.string.user_last_timestamp), FieldValue.serverTimestamp());

        sharedViewModel.userDoc.set(userDetails);

        sharedViewModel.setUserDoc(sharedViewModel.userDoc.get().getResult());

        // Finally open the App with this user
        Log.v(TAG, "Starting CoreApp Activity");

        NavDirections action =
                accountCreationFragDirections.actionAccountCreationFragToCoreFrag();
        Navigation.findNavController(parentView).navigate(action);
    }
}
