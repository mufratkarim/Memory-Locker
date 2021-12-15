package com.mka.memorylocker;

import static com.mka.memorylocker.MemoryLockerActivity.FIRESTORE_MEMORY_REF;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mka.memorylocker.Util.MemoryApi;
import com.mka.memorylocker.Util.MemoryListener;
import com.mka.memorylocker.adapter.MemoryRecyclerAdapter;
import com.mka.memorylocker.model.Memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoryListActivity extends AppCompatActivity implements MemoryListener {

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;
    // Firestore connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private final CollectionReference collectionReference = db.collection(FIRESTORE_MEMORY_REF);
    // Layout Ids
    private List<Memory> memoryList;
    private MemoryRecyclerAdapter memoryRecyclerAdapter;
    private RecyclerView recyclerView;
    private TextView noMemoryText;
    private boolean isChecked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_list);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        memoryList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noMemoryText = findViewById(R.id.noMemories_textView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        MenuItem menuItem = menu.findItem(R.id.my_switch);
        SwitchCompat mySwitch = (SwitchCompat) menuItem.getActionView();

        mySwitch.setOnCheckedChangeListener((buttonView, b) -> {
            // Do something when `isChecked` is true or false
            if (b) {
                isChecked = true;
                Toast.makeText(MemoryListActivity.this, "Select the memory you want to delete", Toast.LENGTH_LONG).show();
            }
            else {
                isChecked = false;
                Toast.makeText(MemoryListActivity.this, "You can no longer delete a memory", Toast.LENGTH_LONG).show();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_addMemory:

                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(MemoryListActivity.this, MemoryLockerActivity.class));
                    finish();
                }
                break;
            case R.id.menu_signOut:

                if (user != null && firebaseAuth != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MemoryListActivity.this);
                    builder.setMessage("Do you want to sign out?")
                            .setTitle("Sign Out")
                            .setPositiveButton("Yes", (dialog, id) -> {
                                firebaseAuth.signOut();
                                startActivity(new Intent(MemoryListActivity.this, MainActivity.class));
                                finish();
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            })
                            .create()
                            .show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", MemoryApi.getInstance()
                .getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        noMemoryText.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot queryMemories : queryDocumentSnapshots) {
                            Memory memory = queryMemories.toObject(Memory.class);
                            memoryList.add(memory);

                        }

                        memoryRecyclerAdapter = new MemoryRecyclerAdapter(MemoryListActivity.this, memoryList, MemoryListActivity.this);
                        recyclerView.setAdapter(memoryRecyclerAdapter);
                        memoryRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        noMemoryText.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                });
    }

    @Override
    public void memoryClicked(Memory memory) {
        if (isChecked) {
            String memoryDocId = memory.getUserName() + memory.getUserId().substring(0,4) + memory.getTitle().substring(memory.getTitle().length()-2) + memory.getMemory().substring(0,3);
            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(memory.getImageUrl());

            AlertDialog.Builder builder = new AlertDialog.Builder(MemoryListActivity.this);
            builder.setMessage("Do you want to delete " + memory.getTitle() + "?")
                    .setTitle("Delete " + memory.getTitle())
                    .setPositiveButton("Yes", (dialog, id) -> {
                        // DELETE
                        memoryList.remove(memory);
                        memoryRecyclerAdapter.notifyDataSetChanged();

                        collectionReference.document(memoryDocId).delete();
                        storageReference.delete()
                                .addOnSuccessListener(unused -> Toast.makeText(MemoryListActivity.this, "Successfully Deleted!", Toast.LENGTH_LONG).show());

                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .create()
                    .show();
        }

    }
}