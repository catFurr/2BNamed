package com.firstproj.a2bnamed.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.firstproj.a2bnamed.CoreApp;
import com.firstproj.a2bnamed.MainActivity;
import com.firstproj.a2bnamed.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;


public class dataUserTab extends Fragment {

    private final String TAG = "dataUserTab";

    private LinearLayout parentLinearLayout;
    private LayoutInflater mInflater;

    interface __interfaceFuncts__ {
        void anyMethod();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_data_user_tab, container, false);
        parentLinearLayout = parentView.findViewById(R.id.dt_cardListHolderView);
        mInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        getLocalCards();
        getOnlineCards();

        return parentView;
    }


    private void getLocalCards() {
        functs origami = new functs();

        parentLinearLayout.addView(
                buildCard("Email Auth",
                        "Checking email",
                        "we sent u a mail",
                        "change mail",
                        "resend mail",
                        false,
                        null,
                        origami::emailResend),
                parentLinearLayout.getChildCount());

        parentLinearLayout.addView(
                buildCard("Become a sharer",
                        "If u have a cycle",
                        "Limited time offer",
                        "contact us",
                        "dismiss",
                        true,
                        null,
                        null),
                parentLinearLayout.getChildCount());

        parentLinearLayout.addView(
                buildCard("Join us",
                        "we r hiring",
                        "quick while it lasts",
                        "find out more",
                        "dismiss",
                        true,
                        null,
                        null),
                parentLinearLayout.getChildCount());

        parentLinearLayout.addView(
                buildCard("", "", "",
                        "Sign Out", "", false,
                        origami::signOut, null),
                parentLinearLayout.getChildCount());
    }

    private void getOnlineCards() {}


    private MaterialCardView buildCard(String title, String description, String subtext,
                                       String action1, String action2, boolean dismissable,
                                       final __interfaceFuncts__  action1_funct, final __interfaceFuncts__ action2_funct) {
        MaterialCardView cardView = (MaterialCardView) mInflater.inflate(R.layout.cardviewtemplate, parentLinearLayout, false);

        ((TextView) cardView.findViewById(R.id.title_cardtemplate)).setText(title);
        ((TextView) cardView.findViewById(R.id.description_cardtemplate)).setText(description);
        ((TextView) cardView.findViewById(R.id.subtext_cardtemplate)).setText(subtext);

        ((MaterialButton) cardView.findViewById(R.id.action1_cardtemplate)).setText(action1);
        ((MaterialButton) cardView.findViewById(R.id.action2_cardtemplate)).setText(action2);

        cardView.findViewById(R.id.action1_cardtemplate).setOnClickListener(view -> {
            // Set this if passed a parameter
            action1_funct.anyMethod();
        });

        cardView.findViewById(R.id.action2_cardtemplate).setOnClickListener(view -> {
            // Set this if passed a parameter
            action2_funct.anyMethod();
        });

        return cardView;
    }


    class functs {
        void emailResend(){
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
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(getActivity(), "Email Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Task failed: " + task.getException());
                        }
                    });
        }

        void signOut() {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            FirebaseAuth.getInstance().signOut();
            startActivity(intent);
            requireActivity().finish();
        }
    }
}
