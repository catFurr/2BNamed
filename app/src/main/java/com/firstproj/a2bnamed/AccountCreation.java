package com.firstproj.a2bnamed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountCreation extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "AccountCreationActivity";

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

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mRollNo = findViewById(R.id.userRollNo);
        mNameFirst = findViewById(R.id.firstName);
        mNameLast = findViewById(R.id.lastName);
        mEmailAddr = findViewById(R.id.emailAddr);

        mNameFirstLastView = findViewById(R.id.firstlastnameview);
        mRollNoView = findViewById(R.id.rollnoview);
        mEmailAddrView = findViewById(R.id.emailAddrView);

        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(this);

        Intent starterIntent = getIntent();
        user = starterIntent.getParcelableExtra(MainActivity.UID_MESSAGE);

        uiState = STATE_NAME;
        updateUI();
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

        String url = "http://named-2empty.firebaseapp.com/verify?uid=" + user.getUid();
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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            makeAccount();
                        } else {
                            Log.d(TAG, "Task failed: " + task.getException());
                        }
                    }
                });
        uiState = STATE_EMAIL_SENT;
        updateUI();
    }

    private void makeAccount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection(getString(R.string.users)).document(user.getUid());

        String nameFirst = mNameFirst.getText().toString();
        String nameLast = mNameLast.getText().toString();
        String rollNo = mRollNo.getText().toString();

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put(getString(R.string.firstNameUser), nameFirst);
        userDetails.put(getString(R.string.lastNameUser), nameLast);
        userDetails.put(getString(R.string.userRollNo), rollNo);
        userDetails.put(getString(R.string.emailVerified), false);
        userDetails.put(getString(R.string.emailId), emailId);
        userDetails.put(getString(R.string.userLoggedInTimes), 0);
        userDetails.put(getString(R.string.lastLoggedInTimestamp), FieldValue.serverTimestamp());
        userDocRef.set(userDetails);

        // Finally open the App with this user
        // Pass along the user too
        Log.v(TAG, "Starting CoreApp Activity");
        Intent intentA = new Intent(this, CoreApp.class);
        intentA.putExtra(MainActivity.UID_MESSAGE, user);
        intentA.putExtra(CoreApp.USER_NAME_FIRST, nameFirst);
        intentA.putExtra(CoreApp.USER_NAME_LAST, nameLast);
        intentA.putExtra(CoreApp.USER_ROLL_NO, rollNo);
        intentA.putExtra(CoreApp.USER_EMAIL_CHECK, false);
        intentA.putExtra(CoreApp.USER_EMAIL, emailId);

        startActivity(intentA);
        finish();
    }

    @Override
    public void onClick(View view) {
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
    }
}
