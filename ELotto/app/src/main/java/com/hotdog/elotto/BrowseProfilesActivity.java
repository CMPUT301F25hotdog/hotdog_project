package com.hotdog.elotto;  // FIXED: Changed from com.hotdog.elotto.ui

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminProfileAdapter;
import com.hotdog.elotto.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Browse and manage profiles - handles your nested User structure
 */
public class BrowseProfilesActivity extends AppCompatActivity {

    private static final String TAG = "BrowseProfiles";

    private ImageView btnBack;
    private EditText searchProfiles;
    private TextView textTotalProfilesCount;
    private RecyclerView recyclerProfiles;
    private LinearLayout emptyState;

    private AdminProfileAdapter profileAdapter;
    private List<User> allProfiles;
    private List<User> filteredProfiles;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_profiles);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadProfiles();
        setupSearchListener();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchProfiles = findViewById(R.id.search_profiles);
        textTotalProfilesCount = findViewById(R.id.text_total_profiles_count);
        recyclerProfiles = findViewById(R.id.recycler_profiles);
        emptyState = findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        allProfiles = new ArrayList<>();
        filteredProfiles = new ArrayList<>();

        profileAdapter = new AdminProfileAdapter(this, filteredProfiles, new AdminProfileAdapter.OnProfileActionListener() {
            @Override
            public void onViewProfile(User user) {
                showProfileDetails(user);
            }

            @Override
            public void onFlagProfile(User user) {
                showFlagDialog(user);
            }

            @Override
            public void onDeleteProfile(User user) {
                showDeleteConfirmationDialog(user);
            }
        });

        recyclerProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerProfiles.setAdapter(profileAdapter);
    }

    private void loadProfiles() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProfiles.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Try to deserialize with Firestore
                            User user = doc.toObject(User.class);
                            allProfiles.add(user);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + doc.getId(), e);
                        }
                    }
                    filteredProfiles.clear();
                    filteredProfiles.addAll(allProfiles);
                    updateUI();
                    Log.d(TAG, "Loaded " + allProfiles.size() + " profiles");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profiles", e);
                    Toast.makeText(this, "Error loading profiles", Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void setupSearchListener() {
        searchProfiles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProfiles(String query) {
        filteredProfiles.clear();

        if (query.isEmpty()) {
            filteredProfiles.addAll(allProfiles);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : allProfiles) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

                if (name.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredProfiles.add(user);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        textTotalProfilesCount.setText("Total Profiles : " + filteredProfiles.size());

        if (filteredProfiles.isEmpty()) {
            recyclerProfiles.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerProfiles.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        profileAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void showProfileDetails(User user) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(user.getName()).append("\n");
        details.append("Email: ").append(user.getEmail()).append("\n");
        details.append("Phone: ").append(user.getPhone()).append("\n");
        details.append("Type: ").append(user.getType()).append("\n");
        details.append("Device ID: ").append(user.getDeviceId()).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Profile Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showFlagDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Flag User")
                .setMessage("Flag \"" + user.getName() + "\" for policy violation?")
                .setPositiveButton("Flag", (dialog, which) -> {
                    Toast.makeText(this, "Flag feature - implement as needed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete \"" + user.getName() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Toast.makeText(this, "Delete feature - implement as needed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles();
    }
}