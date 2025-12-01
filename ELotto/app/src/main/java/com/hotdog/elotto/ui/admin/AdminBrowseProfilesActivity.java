package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminProfileAdapter;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.OrganizerRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Admin Browse Profiles Activity.
 * Implements US 03.05.01 (Browse profiles), US 03.02.01 (Remove profiles),
 * and US 03.07.01 (Remove organizers).
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseProfilesActivity extends AppCompatActivity
        implements AdminProfileAdapter.OnProfileActionListener {

    // Device ID check disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    private RecyclerView recyclerViewProfiles;
    private EditText etSearchProfiles;
    private TextView tvTotalProfiles, tvNoProfiles;
    private ProgressBar progressBar;

    private AdminProfileAdapter adapter;
    private UserRepository userRepository;
    private OrganizerRepository organizerRepository;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Admin Access
        User currentUser = new User(this, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if (user.getType() != UserType.Administrator) {
                    Toast.makeText(getApplicationContext(), "Access Denied: Admin only", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                setContentView(R.layout.activity_admin_browse_profiles);

                initializeViews();
                setupRecyclerView();
                setupSearch();
                loadProfiles();
            }
        });


        // DEVICE ID CHECK REMOVED FOR TESTING
        // String deviceId = Settings.Secure.getString(getContentResolver(),
        // Settings.Secure.ANDROID_ID);
        // if (!ADMIN_DEVICE_ID.equals(deviceId)) {
        // Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
        // finish();
        // return;
        // }


    }

    private void initializeViews() {
        recyclerViewProfiles = findViewById(R.id.rv_admin_profiles);
        etSearchProfiles = findViewById(R.id.et_search_profiles);
        tvTotalProfiles = findViewById(R.id.tv_total_profiles);
        tvNoProfiles = findViewById(R.id.tv_no_profiles);
        progressBar = findViewById(R.id.progress_bar);

        userRepository = UserRepository.getInstance();
        organizerRepository = OrganizerRepository.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(filteredUsers, this);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProfiles.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchProfiles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadProfiles() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoProfiles.setVisibility(View.GONE);

        userRepository.getAllUsers(new com.hotdog.elotto.callback.FirestoreListCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                allUsers.clear();
                allUsers.addAll(users);
                filteredUsers.clear();
                filteredUsers.addAll(users);

                updateUI();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoProfiles.setVisibility(View.VISIBLE);
                tvNoProfiles.setText("Error loading profiles: " + errorMessage);
                Toast.makeText(AdminBrowseProfilesActivity.this, "Failed to load profiles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProfiles(String query) {
        filteredUsers.clear();

        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsers) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) ||
                        (user.getId() != null && user.getId().toLowerCase().contains(lowerQuery))) {
                    filteredUsers.add(user);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        tvTotalProfiles.setText("Total Profiles: " + allUsers.size());

        if (filteredUsers.isEmpty()) {
            tvNoProfiles.setVisibility(View.VISIBLE);
            tvNoProfiles.setText(allUsers.isEmpty() ? "No profiles found" : "No matching profiles");
        } else {
            tvNoProfiles.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewDetails(User user) {
        // Show user details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(user.getName() != null ? user.getName() : "User Profile");

        StringBuilder details = new StringBuilder();
        details.append("Device ID: ").append(user.getId()).append("\n\n");
        details.append("Name: ").append(user.getName() != null ? user.getName() : "N/A").append("\n\n");
        details.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "N/A").append("\n\n");
        details.append("Phone: ").append(user.getPhone() != null ? user.getPhone() : "N/A").append("\n\n");
        details.append("User Type: ").append(user.getType() != null ? user.getType().toString() : "N/A").append("\n\n");
        details.append("Registered Events: ").append(user.getRegEvents() != null ? user.getRegEvents().size() : 0);

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDeleteProfile(User user) {
        String userTypeText = user.getType() != null ? user.getType().toString() : "User";

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete " + userTypeText)
                .setMessage("Are you sure you want to delete this " + userTypeText.toLowerCase()
                        + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRevokeOrganizer(User user) {
        // Show confirmation dialog for revoking organizer status (US 03.07.01)
        new AlertDialog.Builder(this)
                .setTitle("Revoke Organizer Status")
                .setMessage("Are you sure you want to revoke organizer privileges for this user?")
                .setPositiveButton("Revoke", (dialog, which) -> revokeOrganizerStatus(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile(User user) {
        progressBar.setVisibility(View.VISIBLE);

        // If user is an organizer, also delete from organizers collection
        if (user.getType() == UserType.Organizer) {
            organizerRepository.deleteOrganizer(user.getId(), new com.hotdog.elotto.callback.OperationCallback() {
                @Override
                public void onSuccess() {
                    // After organizer deleted, delete user profile
                    deleteUserProfile(user);
                }

                @Override
                public void onError(String errorMessage) {
                    // Even if organizer deletion fails, try to delete user
                    deleteUserProfile(user);
                }
            });
        } else {
            deleteUserProfile(user);
        }
    }

    private void deleteUserProfile(User user) {
        userRepository.deleteUser(user.getId(), new com.hotdog.elotto.callback.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                allUsers.remove(user);
                filteredUsers.remove(user);
                updateUI();
                Toast.makeText(AdminBrowseProfilesActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminBrowseProfilesActivity.this, "Failed to delete profile: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revokeOrganizerStatus(User user) {
        progressBar.setVisibility(View.VISIBLE);

        // Delete from organizers collection
        organizerRepository.deleteOrganizer(user.getId(), new com.hotdog.elotto.callback.OperationCallback() {
            @Override
            public void onSuccess() {
                // Update user type to Entrant
                user.setType(UserType.Entrant);
                userRepository.updateUser(user, new com.hotdog.elotto.callback.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        updateUI();
                        Toast.makeText(AdminBrowseProfilesActivity.this, "Organizer status revoked successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminBrowseProfilesActivity.this, "Failed to update user type",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminBrowseProfilesActivity.this, "Failed to revoke organizer status: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}