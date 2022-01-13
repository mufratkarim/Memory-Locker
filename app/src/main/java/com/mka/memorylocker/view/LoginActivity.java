package com.mka.memorylocker.view;

import static com.mka.memorylocker.view.CreateAccountActivity.KEY_USER_ID;
import static com.mka.memorylocker.view.CreateAccountActivity.KEY_USER_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mka.memorylocker.R;
import com.mka.memorylocker.controller.Util.MemoryApi;
import com.mka.memorylocker.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    public static final String FIRESTORE_USER_REF = "Users";
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser currentUser;
    // Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference reference = db.collection(FIRESTORE_USER_REF);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        listener = firebaseAuth -> currentUser = firebaseAuth.getCurrentUser();

        loginBinding.buttonCreate.setOnClickListener(view -> {
            startActivity(new Intent(this, CreateAccountActivity.class));
        });

        loginBinding.buttonLogin.setOnClickListener(view -> {

            String email = loginBinding.email.getText().toString().trim();
            String password = Objects.requireNonNull(loginBinding.password.getText()).toString().trim();

            login(email, password);

        });
    }

    private void login(String email, String password) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            loginBinding.progress.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this, authResult -> {
                        currentUser = firebaseAuth.getCurrentUser();
                        String userId = Objects.requireNonNull(currentUser).getUid();

                        reference
                                .whereEqualTo(KEY_USER_ID, userId)
                                .addSnapshotListener((value, error) -> {

                                    if (error != null) {
                                        Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    if (!Objects.requireNonNull(value).isEmpty()) {

                                        loginBinding.progress.setVisibility(View.GONE);

                                        for (QueryDocumentSnapshot snapshot : value) {
                                            MemoryApi memoryApi = MemoryApi.getInstance();
                                            memoryApi.setUser(snapshot.getString(KEY_USER_NAME));
                                            memoryApi.setUserId(snapshot.getString(KEY_USER_ID));
                                            memoryApi.setDocId(snapshot.getId());

                                            startActivity(new Intent(LoginActivity.this, MemoryListActivity.class));
                                            finish();
                                        }
                                    }
                                });
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loginBinding.progress.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        currentUser = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }
}