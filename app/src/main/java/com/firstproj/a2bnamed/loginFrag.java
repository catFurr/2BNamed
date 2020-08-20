package com.firstproj.a2bnamed;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.firstproj.a2bnamed.adapter.viewmodel;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class loginFrag extends Fragment implements View.OnClickListener{

    public loginFrag() {
        // Required empty public constructor
    }

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

    private View parentView;
    private viewmodel sharedViewModel;

    private ViewGroup mPhoneLoginView;
    private ViewGroup mPhoneVerificationView;
    private EditText mPhoneNumberField;
    private EditText mCountryCode;
    private EditText mVerificationField;
    private Button mVerifyButton;
    private Button mSendNumButton;
    private Button mResendCodeButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.activity_login_account, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(viewmodel.class);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views
        mPhoneLoginView = parentView.findViewById(R.id.phoneloginview);
        mPhoneVerificationView = parentView.findViewById(R.id.phoneverificationview);

        mPhoneNumberField = parentView.findViewById(R.id.phoneNo);
        mCountryCode = parentView.findViewById(R.id.countryCode);
        mVerificationField = parentView.findViewById(R.id.verificationCode);

        mVerifyButton = parentView.findViewById(R.id.codeverifybutton);
        mSendNumButton = parentView.findViewById(R.id.phoneNoSendButton);
        mResendCodeButton = parentView.findViewById(R.id.resendCodeBttn);

        // Assign click listeners
        mVerifyButton.setOnClickListener(this);
        mSendNumButton.setOnClickListener(this);
        mResendCodeButton.setOnClickListener(this);

        // Initialize phone auth callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
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
            public void onVerificationFailed(@NotNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    mPhoneNumberField.setError(getString(R.string.la_invalid_no));
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getActivity(), getString(R.string.la_sms_exceeded),
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

        return parentView;
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
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    private void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }



    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                requireActivity(),  // Activity (for callback binding)
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
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                requireActivity(),  // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        updateUI(STATE_SIGNIN_SUCCESS);
                        checkForAccount(Objects.requireNonNull(task.getResult()));
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            mVerificationField.setError(getString(R.string.la_invalid_code));
                        }
                        // Update UI
                        updateUI(STATE_SIGNIN_FAILED);
                    }
                });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError(getString(R.string.la_invalid_no));
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
                    mVerificationField.setError(getString(R.string.la_empty_err));
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.resendCodeBttn:
                resendVerificationCode(fullPhoneNumber, mResendToken);
                break;
        }
    }

    private void checkForAccount(AuthResult result) {
        sharedViewModel.setUserUid(Objects.requireNonNull(result.getUser()).getUid());

        if (Objects.requireNonNull(result.getAdditionalUserInfo()).isNewUser()) {
            // do create new user
            // Logged In already, but does not have any data on server
            Log.v(TAG, "Starting AccountCreation Activity");
            NavDirections action =
                    loginFragDirections.actionLoginFragToAccountCreationFrag();
            Navigation.findNavController(parentView).navigate(action);
        } else {
            // Logged In
            sharedViewModel.db = FirebaseFirestore.getInstance();
            sharedViewModel.userDoc =
                    sharedViewModel.db.collection(getString(R.string.fb_users)).document(sharedViewModel.getUserUid());

            sharedViewModel.userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    if (doc.exists()) {
                        Log.v(TAG, "Starting Core App Activity");

                        sharedViewModel.setUserDoc(doc);

                        Navigation.findNavController(parentView).navigate(R.id.coreFrag);
                    } else {
                        Log.v(TAG, "Starting AccountCreation Activity");
                        NavDirections action =
                                loginFragDirections.actionLoginFragToAccountCreationFrag();
                        Navigation.findNavController(parentView).navigate(action);
                    }
                } else {
                    // Report an error
                    Log.e(TAG, "user Doc fetch failed.");
                }
            });
        }
    }
}
