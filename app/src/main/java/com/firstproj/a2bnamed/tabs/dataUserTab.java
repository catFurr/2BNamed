package com.firstproj.a2bnamed.tabs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.firstproj.a2bnamed.CoreFragDirections;
import com.firstproj.a2bnamed.R;
import com.firstproj.a2bnamed.adapter.viewmodel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class dataUserTab extends Fragment {

    private final String TAG = "dataUserTab";

    private viewmodel sharedViewModel;
    private LinearLayout parentLinearLayout;
    private LayoutInflater mInflater;
    private View parentView;

    interface __interfaceFuncts__ {
        void anyMethod();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (parentView != null) return parentView;

        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_data_user_tab, container, false);
        parentLinearLayout = parentView.findViewById(R.id.dt_cardListHolderView);
        mInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(viewmodel.class);

        getOnlineCards();
        getLocalCards();

        TextView bottomInfoLegal = new TextView(requireContext());
        bottomInfoLegal.setText(R.string.dt_spacer_terms);
        parentLinearLayout.addView(
                bottomInfoLegal,
                parentLinearLayout.getChildCount());

        return parentView;
    }


    private void getLocalCards() {
        functs origami = new functs();

        parentLinearLayout.addView(
                buildCard("Email Verification",
                        "An email has been sent to your NITK email ID. " +
                                "Please click on the link present in the mail to begin.",
                        "Can't find it? Check your spam or tap resend.",
                        "Change Email ID",
                        "Resend Mail",
                        origami::changeUserEmailID,
                        origami::emailResend),
                parentLinearLayout.getChildCount());

        /*sharedViewModel.userEmailVerified.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {

            }
        });*/

        parentLinearLayout.addView(
                buildCard("Become a Sharer!",
                        "Our unique business model ensures that our growth helps the entire " +
                                "community grow as well.",
                        "This is an exciting limited time offer for cycle-owners to reap benefits.",
                        "Find Out More",
                        "",
                        origami::openLink,
                        null),
                parentLinearLayout.getChildCount());

        parentLinearLayout.addView(
                buildCard("Join Us",
                        "2BNamed is now hiring! Our employees get huge benefits, apart from " +
                                "the amazing learning experience.",
                        "Early birds get the best worms!",
                        "Contact Us",
                        "",
                        origami::openLink,
                        null),
                parentLinearLayout.getChildCount());

        parentLinearLayout.addView(
                buildCard("", "", "",
                        "Sign Out", "",
                        origami::signOut, null),
                parentLinearLayout.getChildCount());
    }

    private void getOnlineCards() {}


    private MaterialCardView buildCard(String title, String description, String subtext,
                                       String action1, String action2,
                                       final __interfaceFuncts__  action1_funct, final __interfaceFuncts__ action2_funct) {
        MaterialCardView cardView = (MaterialCardView) mInflater.inflate(R.layout.cardviewtemplate, parentLinearLayout, false);

        if (!Objects.equals(title, "")) {
            ((TextView) cardView.findViewById(R.id.title_cardtemplate)).setText(title);
        } else {
            ((ViewGroup) cardView.findViewById(R.id.title_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.title_cardtemplate));
        }

        if (!Objects.equals(description, "")) {
            ((TextView) cardView.findViewById(R.id.description_cardtemplate)).setText(description);
        } else {
            ((ViewGroup) cardView.findViewById(R.id.description_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.description_cardtemplate));
        }

        if (!Objects.equals(subtext, "")) {
            ((TextView) cardView.findViewById(R.id.subtext_cardtemplate)).setText(subtext);
        } else {
            ((ViewGroup) cardView.findViewById(R.id.subtext_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.subtext_cardtemplate));
        }

        if (((LinearLayout) cardView.findViewById(R.id.textlayout_cardtemplate)).getChildCount() == 0) {
            ((ViewGroup) cardView.findViewById(R.id.textlayout_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.textlayout_cardtemplate));
        }


        if (!Objects.equals(action1, "") && action1_funct != null) {
            ((MaterialButton) cardView.findViewById(R.id.action1_cardtemplate)).setText(action1);
            cardView.findViewById(R.id.action1_cardtemplate).setOnClickListener(view -> {
                // Set this if passed a parameter
                action1_funct.anyMethod();
            });
        } else {
            ((ViewGroup) cardView.findViewById(R.id.action1_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.action1_cardtemplate));
        }

        if (!Objects.equals(action2, "") && action2_funct != null) {
            ((MaterialButton) cardView.findViewById(R.id.action2_cardtemplate)).setText(action2);
            cardView.findViewById(R.id.action2_cardtemplate).setOnClickListener(view -> {
                // Set this if passed a parameter
                action2_funct.anyMethod();
            });
        } else {
            ((ViewGroup) cardView.findViewById(R.id.action2_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.action2_cardtemplate));
        }

        if (((LinearLayout) cardView.findViewById(R.id.actionslayout_cardtemplate)).getChildCount() == 0) {
            ((ViewGroup) cardView.findViewById(R.id.actionslayout_cardtemplate).getParent())
                    .removeView(cardView.findViewById(R.id.actionslayout_cardtemplate));
        }

        return cardView;
    }


    class functs {
        void emailResend(){
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
            auth.sendSignInLinkToEmail(Objects.requireNonNull(sharedViewModel.userEmailId.getValue()), actionCodeSettings)
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
            FirebaseAuth.getInstance().signOut();
            NavDirections action =
                    CoreFragDirections.actionCoreFragToLoginFrag();
            Navigation.findNavController(parentLinearLayout).navigate(action);
        }

        void changeUserEmailID () {
            NavDirections action =
                    CoreFragDirections.actionCoreFragToSettingsFrag();
            Navigation.findNavController(parentLinearLayout).navigate(action);
        }

        void openLink() {
        }
    }
}