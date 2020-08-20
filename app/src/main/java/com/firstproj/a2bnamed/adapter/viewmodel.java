package com.firstproj.a2bnamed.adapter;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class viewmodel extends ViewModel {

    private String userUid;
    public MutableLiveData<String> userEmailId = new MutableLiveData<>();
    public MutableLiveData<String> userFirstName = new MutableLiveData<>();
    public MutableLiveData<String> userLastName = new MutableLiveData<>();
    public MutableLiveData<String> userRollNo = new MutableLiveData<>();
    public MutableLiveData<Boolean> userEmailVerified = new MutableLiveData<>();

    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public DocumentReference userDoc;
    private MutableLiveData<DocumentSnapshot> doc = new MutableLiveData<>();
    public FirebaseFirestore db;

    public void setUserUid (String value) {
        this.userUid = value;
    }
    public String getUserUid () {
        return this.userUid;
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public LiveData<DocumentSnapshot> getUserDoc() { return this.doc;  }
    public void setUserDoc(DocumentSnapshot val) {
        if (isMainThread()) doc.setValue(val);
        else doc.postValue(val);
    }

}
