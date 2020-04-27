package com.firstproj.a2bnamed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tMainActivity";
    public static final String UID_MESSAGE = "com.firstproj.a2bnamed.uid_message";
    public static final String EMAIL_LINK_MESSAGE = "com.a2bnamed.email_link_message";

    private FirebaseUser user;
    DocumentReference userDoc;

    private boolean canGoAhead = false;
    private boolean isLinkPresent = false;
    private String emailLink;

    private Intent intent;

    private DocumentSnapshot doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Intent starterIntent = getIntent();
        user = starterIntent.getParcelableExtra(UID_MESSAGE);
        if (user == null) {
            user = mAuth.getCurrentUser();
        }

        emailLink = starterIntent.getStringExtra(EMAIL_LINK_MESSAGE);
        if (emailLink != null) isLinkPresent = true;

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }


                        if (deepLink != null){
                            Log.d(TAG, "Deep Link: " + deepLink.toString());
                            FirebaseAuth auth = FirebaseAuth.getInstance();

                            emailLink = deepLink.toString();
                            // Confirm the link is a sign-in with email link.
                            if (auth.isSignInWithEmailLink(emailLink)) {
                                // Retrieve the emailID from wherever you stored it
                                isLinkPresent = true;
                            }
                        }

                        canGoAhead = true;
                        switchToAct();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                        canGoAhead = true;
                        switchToAct();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            //Not Logged In or Not Email verified
            startLoginAct();
        } else {
            // Logged In, Account Present
            // Get user deets from firestore

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            userDoc = db.collection(getString(R.string.users)).document(user.getUid());

            userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        doc = task.getResult();
                        if (doc.exists()) {
                            startCoreApp();
                        } else {
                            startAccountCreationAct();
                        }
                    } else {
                        startLoginAct();
                    }
                }
            });
        }
    }

    private void startCoreApp() {
        Log.v(TAG, "Starting Core App Activity");
        intent = new Intent(MainActivity.this, CoreApp.class);
        intent.putExtra(UID_MESSAGE, user);
        intent.putExtra(CoreApp.USER_NAME_FIRST, doc.getString(getString(R.string.firstNameUser)));
        intent.putExtra(CoreApp.USER_NAME_LAST, doc.getString(getString(R.string.lastNameUser)));
        intent.putExtra(CoreApp.USER_ROLL_NO, doc.getString(getString(R.string.userRollNo)));
        intent.putExtra(CoreApp.USER_EMAIL_CHECK, doc.getBoolean(getString(R.string.emailVerified)));
        intent.putExtra(CoreApp.USER_EMAIL, doc.getString(getString(R.string.emailId)));

        switchToAct();
    }

    private void startLoginAct() {
        Log.v(TAG, "Starting loginAccount Activity");
        intent = new Intent(MainActivity.this, loginAccount.class);

        switchToAct();
    }

    private void startAccountCreationAct() {
        Log.v(TAG, "Starting AccountCreation Activity");
        intent = new Intent(MainActivity.this, AccountCreation.class);
        intent.putExtra(UID_MESSAGE, user);

        switchToAct();
    }


    private void switchToAct() {
        if (intent == null) return;
        if (!canGoAhead) return;

        if (isLinkPresent) {
            if (user == null || !doc.exists()) {
                // Pass along the link. We might get it back
                intent.putExtra(EMAIL_LINK_MESSAGE, emailLink);
                startActivity(intent);
                finish();
            } else {
                // WARNING : Cuz doc might exist but not have email ID field (unlikely)
                // Construct the email link credential from the current URL.
                AuthCredential credential =
                        EmailAuthProvider.getCredentialWithLink(doc.getString(getString(R.string.emailId)), emailLink);

                // Link the credential to the current user.
                user.linkWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Successfully linked emailLink credential!");
                                    userDoc.update(getString(R.string.emailVerified), true);
                                    intent.removeExtra(CoreApp.USER_EMAIL_CHECK);
                                    intent.putExtra(CoreApp.USER_EMAIL_CHECK, true);
                                } else {
                                    Log.e(TAG, "Error linking emailLink credential", task.getException());
                                }
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        } else {
            startActivity(intent);
            finish();
        }


    }

}