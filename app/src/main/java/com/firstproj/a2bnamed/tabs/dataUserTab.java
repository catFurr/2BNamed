package com.firstproj.a2bnamed.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.firstproj.a2bnamed.CoreApp;
import com.firstproj.a2bnamed.MainActivity;
import com.firstproj.a2bnamed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;


public class dataUserTab extends Fragment
        implements View.OnClickListener {

    private final String TAG = "dataUserTab";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_user_tab, container, false);

        view.findViewById(R.id.bttnSignOut).setOnClickListener(this);
        view.findViewById(R.id.bttnEmailResend).setOnClickListener(this);

        return view;
    }

    private void emailResend(){
        String url = "http://named-2empty.firebaseapp.com/verify?uid=" + CoreApp.user.getUid();
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
        auth.sendSignInLinkToEmail(CoreApp.userEmailID, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(getActivity(), "Email Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Task failed: " + task.getException());
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bttnSignOut:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.bttnEmailResend:
                emailResend();
                break;
            default:
                break;
        }
    }
}
