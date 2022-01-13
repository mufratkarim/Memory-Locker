package com.mka.memorylocker.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mka.memorylocker.R;
import com.mka.memorylocker.databinding.ActivityMemoryLockerBinding;
import com.mka.memorylocker.controller.Util.MemoryApi;
import com.mka.memorylocker.model.Memory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MemoryLockerActivity extends AppCompatActivity {

    private ActivityMemoryLockerBinding memoryBinding;
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;
    // Firestore connection
    public static final String FIRESTORE_MEMORY_REF = "Memories";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference memoryReference = db.collection(FIRESTORE_MEMORY_REF);

    // Current User
    private String currentUser;
    private String userId;
    private String docId;

    private Uri imageUri = Uri.parse(MemoryApi.get().getString(R.string.location_default_image));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memoryBinding = DataBindingUtil.setContentView(this, R.layout.activity_memory_locker);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        // Default Image URI and date
        memoryBinding.lockerDateTextView.setText(new SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(new Date()));
        memoryBinding.lockerImageView.setImageURI(imageUri);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        listener = firebaseAuth -> user = firebaseAuth.getCurrentUser();

        // GetContent creates an ActivityResultLauncher<String> to allow you to pass
        // in the mime type you'd like to allow the user to select
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        imageUri = uri;
                        memoryBinding.lockerImageView.setImageURI(imageUri);
                    }
                });

        if (MemoryApi.getInstance() != null) {
            currentUser = MemoryApi.getInstance().getUser();
            userId = MemoryApi.getInstance().getUserId();
            docId = MemoryApi.getInstance().getDocId();

            memoryBinding.lockerUserNameTextView.setText(currentUser);
        }

        memoryBinding.lockerCameraButton.setOnClickListener(view -> mGetContent.launch("image/*"));

        memoryBinding.lockerSaveButton.setOnClickListener(view -> {

            memoryBinding.lockerProgressBar.setVisibility(View.VISIBLE);

            String title = memoryBinding.lockerTitleEditText.getText().toString().trim();
            String description = memoryBinding.lockerDescriptionEditText.getText().toString().trim();

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {

                StorageReference filePath = storageReference.child("memory_photos")
                        .child(docId + "_" + title + description.length());

                filePath.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Create a memory object

                            filePath.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Memory memory = new Memory();
                                        memory.setTitle(title);
                                        memory.setMemory(description);
                                        memory.setUserName(currentUser);
                                        memory.setUserId(userId);
                                        memory.setTimeAdded(new Timestamp(new Date()));
                                        memory.setImageUrl(uri.toString());

                                        // Invoke the memory object on firebase
                                        invokeMemory(memory);

                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MemoryLockerActivity.this, "Download Url Extraction failed!", Toast.LENGTH_SHORT).show();
                                    });

                        })
                .addOnFailureListener(e -> {
                    memoryBinding.lockerProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MemoryLockerActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_locker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_accessList:
                startActivity(new Intent(MemoryLockerActivity.this, MemoryListActivity.class));
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void invokeMemory(Memory memory) {

        String memoryDocId = memory.getUserName() + memory.getUserId().substring(0,4) + memory.getTitle().substring(memory.getTitle().length()-2) + memory.getMemory().substring(0,3);
        memoryReference.document(memoryDocId).set(memory)
                .addOnSuccessListener(documentReference -> {
                    memoryBinding.lockerProgressBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(MemoryLockerActivity.this, MemoryListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MemoryLockerActivity.this, "Failed to add Memory into firebase!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        user = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }
}