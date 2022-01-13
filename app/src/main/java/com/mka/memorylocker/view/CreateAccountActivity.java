package com.mka.memorylocker.view;

import static com.mka.memorylocker.view.LoginActivity.FIRESTORE_USER_REF;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mka.memorylocker.R;
import com.mka.memorylocker.databinding.ActivityCreateAccountBinding;
import com.mka.memorylocker.controller.Util.MemoryApi;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    private ActivityCreateAccountBinding accountBinding;
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;
    // Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference reference = db.collection(FIRESTORE_USER_REF);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        listener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
        };

        accountBinding.actButtonCreate.setOnClickListener(view -> {

            String mEmail = accountBinding.actEmail.getText().toString().trim();
            String mPassword = Objects.requireNonNull(accountBinding.actPassword.getText()).toString().trim();
            String mUsername = Objects.requireNonNull(accountBinding.userName.getText()).toString().trim();

            createAccount(mEmail, mPassword, mUsername);

        });
    }

    private void createAccount(String email, String password, String userName) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(userName)) {
            accountBinding.actProgress.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            String userId = Objects.requireNonNull(user).getUid();
                            String docId = userName.substring(0,3) + userId.substring(userId.length() - 4);

                            MemoryApi memoryApi = MemoryApi.getInstance();
                            memoryApi.setDocId(docId);
                            memoryApi.setUser(userName);
                            memoryApi.setUserId(userId);

                            Map<String, String> userObj = new HashMap<>();
                            userObj.put(KEY_USER_ID, userId);
                            userObj.put(KEY_USER_NAME, userName);

                            // save to database
                            reference.document(docId).set(userObj)
                                    .addOnSuccessListener(this, unused -> {
                                        accountBinding.actProgress.setVisibility(View.GONE);
                                        Intent intent = new Intent(CreateAccountActivity.this, MemoryLockerActivity.class);
                                        intent.putExtra(KEY_USER_ID, userId);
                                        intent.putExtra(KEY_USER_NAME, userName);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(this, e -> Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(CreateAccountActivity.this, "Fill out the fields properly", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
    }
}