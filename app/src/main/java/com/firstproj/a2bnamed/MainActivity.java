package com.firstproj.a2bnamed;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.firstproj.a2bnamed.adapter.viewmodel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tMainActivity";

    private SparseArray<Fragment.SavedState> savedStateSparseArray = new SparseArray<>();
    static final String SAVED_STATE_CONTAINER_KEY = "ContainerKey";
    static final String SAVED_STATE_CURRENT_TAB_KEY = "CurrentTabKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*        if (savedInstanceState != null) {
            savedStateSparseArray = savedInstanceState.getSparseParcelableArray(SAVED_STATE_CONTAINER_KEY);
            currentSelectItemId = savedInstanceState.getInt(SAVED_STATE_CURRENT_TAB_KEY);
        }*/

        viewmodel sharedViewModel = new ViewModelProvider(this).get(viewmodel.class);

        sharedViewModel.getUserDoc().observe(this, documentSnapshot -> {
            sharedViewModel.userFirstName.setValue(documentSnapshot.getString(getString(R.string.user_first_name)));
            sharedViewModel.userLastName.setValue(documentSnapshot.getString(getString(R.string.user_last_name)));
            sharedViewModel.userRollNo.setValue(documentSnapshot.getString(getString(R.string.user_roll_no)));
            sharedViewModel.userEmailVerified.setValue(documentSnapshot.getBoolean(getString(R.string.user_email_verified)));
            sharedViewModel.userEmailId.setValue(documentSnapshot.getString(getString(R.string.user_email_id)));
        });

        sharedViewModel.mAuth = FirebaseAuth.getInstance();
        sharedViewModel.user = sharedViewModel.mAuth.getCurrentUser();

        if (sharedViewModel.user == null) {
            //Not Logged In or Not Email verified
            Log.v(TAG, "Starting loginAccount Activity");

            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_main);
            NavDirections action =
                    CoreFragDirections.actionCoreFragToLoginFrag();
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action);
        } else {
            // Logged In, Account Present
            // Get user details from FireStore
            sharedViewModel.setUserUid(sharedViewModel.user.getUid());

            sharedViewModel.db = FirebaseFirestore.getInstance();
            sharedViewModel.userDoc = sharedViewModel.db.collection(getString(R.string.fb_users)).document(sharedViewModel.getUserUid());

            sharedViewModel.userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    if (doc.exists()) {
                        Log.v(TAG, "Starting Core App Activity");

                        sharedViewModel.setUserDoc(doc);
                        setTheme(R.style.AppTheme);
                        setContentView(R.layout.activity_main);
                    } else {
                        Log.v(TAG, "Starting AccountCreation Activity");

                        setTheme(R.style.AppTheme);
                        setContentView(R.layout.activity_main);
                        NavDirections action =
                                CoreFragDirections.actionCoreFragToAccountCreationFrag();
                        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action);
                    }
                } else {
                    // Report an error
                    Log.e(TAG, "user Doc fetch failed. Possible Server side security issue?");

                    // TODO use SharedPref file here
                    finish();
                }
            });
        }
    }
}