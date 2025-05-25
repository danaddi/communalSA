package com.example.test_app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.test_app.models.UtilityData;

public class FirebaseHelper {
    private DatabaseReference database;

    public FirebaseHelper() {
        database = FirebaseDatabase.getInstance().getReference("utilities");
    }

    public void saveUtilityData(UtilityData data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String key = database.child(uid).push().getKey();
            if (key != null) {
                database.child(uid).child(key).setValue(data);
            }
        }
    }
}