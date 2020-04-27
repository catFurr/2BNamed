package com.firstproj.a2bnamed;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class loginAccount extends AppCompatActivity implements
        View.OnClickListener{

    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "com.a2bnamed.key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;


    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneLoginView;
    private ViewGroup mPhoneVerificationView;
    private EditText mPhoneNumberField;
    private EditText mCountryCode;
    private EditText mVerificationField;
    private Button mVerifyButton;
    private Button mSendNumButton;
    private Button mResendCodeButton;

    private String emailLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_account);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views
        mPhoneLoginView = findViewById(R.id.phoneloginview);
        mPhoneVerificationView = findViewById(R.id.phoneverificationview);

        mPhoneNumberField = findViewById(R.id.phoneNo);
        mCountryCode = findViewById(R.id.countryCode);
        mVerificationField = findViewById(R.id.verificationCode);

        mVerifyButton = findViewById(R.id.codeverifybutton);
        mSendNumButton = findViewById(R.id.phoneNoSendButton);
        mResendCodeButton = findViewById(R.id.resendCodeBttn);

        // Assign click listeners
        mVerifyButton.setOnClickListener(this);
        mSendNumButton.setOnClickListener(this);
        mResendCodeButton.setOnClickListener(this);


        emailLink = getIntent().getStringExtra(MainActivity.EMAIL_LINK_MESSAGE);

        // Initialize phone auth callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;

                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    mPhoneNumberField.setError("Invalid phone number");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(loginAccount.this, "SMS Quota exceeded",
                            Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // Update UI
                updateUI(STATE_CODE_SENT);
            }
        };

        updateUI(STATE_INITIALIZED);
    }

    @Override
    public void onStart() {
        super.onStart();

        String fullPhoneNumber = mCountryCode.getText().toString() +
                mPhoneNumberField.getText().toString();
        if (mVerificationInProgress && validatePhoneNumber(fullPhoneNumber)) {
            startPhoneNumberVerification(fullPhoneNumber);
            updateUI(STATE_CODE_SENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }



    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            updateUI(STATE_SIGNIN_SUCCESS);
                            checkForAccount(task.getResult());
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mVerificationField.setError("Invalid code");
                            }
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number");
            return false;
        }

        return true;
    }


    private void updateUI(int uiState) {
        switch (uiState) {
            case STATE_INITIALIZED:
            case STATE_VERIFY_FAILED:
                enableViews(mPhoneNumberField, mCountryCode,
                        mSendNumButton);
                disableViews(mVerificationField, mVerifyButton,
                        mResendCodeButton);
                mPhoneLoginView.setVisibility(View.VISIBLE);
                mPhoneVerificationView.setVisibility(View.GONE);
                break;
            case STATE_CODE_SENT:
            case STATE_SIGNIN_FAILED:
                // Verification has failed, show all options
                // No-op, handled by sign-in check
                // Code sent state, show the verification field, the
                enableViews(mVerifyButton, mVerificationField,
                        mResendCodeButton);
                disableViews(mPhoneNumberField, mCountryCode,
                        mSendNumButton);
                mPhoneLoginView.setVisibility(View.GONE);
                mPhoneVerificationView.setVisibility(View.VISIBLE);
                break;
            case STATE_VERIFY_SUCCESS:
            case STATE_SIGNIN_SUCCESS:
                // Verification has succeeded, proceed to FireBase sign in
                disableViews(mVerifyButton, mPhoneNumberField,
                        mVerificationField, mCountryCode,
                        mSendNumButton, mResendCodeButton);
                mPhoneLoginView.setVisibility(View.GONE);
                mPhoneVerificationView.setVisibility(View.GONE);
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

    @Override
    public void onClick(View view) {
        String fullPhoneNumber = mCountryCode.getText().toString() +
                mPhoneNumberField.getText().toString();
        switch (view.getId()) {
            case R.id.phoneNoSendButton:
                if (!validatePhoneNumber(fullPhoneNumber)) {
                    return;
                }
                startPhoneNumberVerification(fullPhoneNumber);
                break;
            case R.id.codeverifybutton:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.resendCodeBttn:
                resendVerificationCode(fullPhoneNumber, mResendToken);
                break;
        }
    }

    public void checkForAccount(AuthResult result) {
        Intent intent;

        if (result.getAdditionalUserInfo().isNewUser()) {
            //do create new user
            // Logged In already, but does not have any data on server
            Log.v(TAG, "Starting AccountCreation Activity");
            intent = new Intent(loginAccount.this, AccountCreation.class);
            intent.putExtra(MainActivity.UID_MESSAGE, result.getUser());
        } else {
            // Logged In
            // Lets restart the app, for MainActivity to downlink user data
            Log.v(TAG, "Starting SplashScreen Activity");
            intent = new Intent(loginAccount.this, MainActivity.class);
            if (emailLink != null) {
                intent.putExtra(MainActivity.EMAIL_LINK_MESSAGE, emailLink);
            }
        }

        startActivity(intent);
        finish();
    }

}
