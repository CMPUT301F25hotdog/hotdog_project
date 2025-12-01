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
 * Activity for administrators to browse and manage user profiles.
 *
 * <p>This activity provides administrators with the ability to view all user profiles,
 * search/filter profiles by name, email, or device ID, view detailed user information,
 * delete user profiles, and revoke organizer privileges from users. Access is restricted
 * to users with Administrator privileges.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>View all user profiles in the system</li>
 *     <li>Real-time search filtering across name, email, and device ID</li>
 *     <li>User details dialog showing comprehensive profile information</li>
 *     <li>Profile deletion with confirmation dialog and cascade deletion for organizers</li>
 *     <li>Organizer privilege revocation with automatic user type update</li>
 *     <li>Total profile count display</li>
 *     <li>Loading indicators and empty state handling</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseProfilesActivity extends AppCompatActivity
        implements AdminProfileAdapter.OnProfileActionListener {

    // Device ID check disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    /**
     * RecyclerView for displaying the list of user profiles.
     */
    private RecyclerView recyclerViewProfiles;

    /**
     * EditText for search/filter input.
     */
    private EditText etSearchProfiles;

    /**
     * TextView displaying the total number of profiles.
     */
    private TextView tvTotalProfiles;

    /**
     * TextView displayed when no profiles are found or match the search query.
     */
    private TextView tvNoProfiles;

    /**
     * ProgressBar shown during loading operations.
     */
    private ProgressBar progressBar;

    /**
     * Adapter for binding user profile data to the RecyclerView.
     */
    private AdminProfileAdapter adapter;

    /**
     * Repository for user data access operations.
     */
    private UserRepository userRepository;

    /**
     * Repository for organizer data access operations.
     */
    private OrganizerRepository organizerRepository;

    /**
     * Complete list of all users loaded from Firestore.
     */
    private List<User> allUsers = new ArrayList<>();

    /**
     * Filtered list of users based on search query.
     */
    private List<User> filteredUsers = new ArrayList<>();

    /**
     * Called when the activity is starting.
     *
     * <p>Verifies that the current user has Administrator privileges before
     * initializing the activity. If access is denied, displays a toast message
     * and finishes the activity.</p>
     *
     * @param savedInstanceState the saved instance state Bundle
     */
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

    }

    /**
     * Initializes all view components and repositories.
     *
     * <p>Binds UI elements by their IDs and obtains singleton instances of
     * UserRepository and OrganizerRepository for data operations.</p>
     */
    private void initializeViews() {
        recyclerViewProfiles = findViewById(R.id.rv_admin_profiles);
        etSearchProfiles = findViewById(R.id.et_search_profiles);
        tvTotalProfiles = findViewById(R.id.tv_total_profiles);
        tvNoProfiles = findViewById(R.id.tv_no_profiles);
        progressBar = findViewById(R.id.progress_bar);

        userRepository = UserRepository.getInstance();
        organizerRepository = OrganizerRepository.getInstance();
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     *
     * <p>Creates an AdminProfileAdapter with the filtered users list and sets
     * this activity as the action listener for handling profile actions.</p>
     */
    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(filteredUsers, this);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProfiles.setAdapter(adapter);
    }

    /**
     * Sets up the search functionality with real-time text filtering.
     *
     * <p>Adds a TextWatcher to the search EditText that filters profiles as the
     * user types, providing instant search results.</p>
     */
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

    /**
     * Loads all user profiles from Firestore.
     *
     * <p>Shows a progress bar during loading and updates the UI with the loaded
     * profiles on success. Displays an error message if loading fails.</p>
     */
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

    /**
     * Filters user profiles based on the search query.
     *
     * <p>Performs a case-insensitive search across user name, email, and device ID
     * fields. If the query is empty, displays all profiles. Updates the UI with
     * the filtered results.</p>
     *
     * @param query the search query string
     */
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

    /**
     * Updates the UI based on current user profile data.
     *
     * <p>Updates the total profiles count, shows/hides the "no profiles" message
     * appropriately, and notifies the adapter of data changes.</p>
     */
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

    /**
     * Handles the view details action for a user profile.
     *
     * <p>Displays an AlertDialog with comprehensive user information including
     * device ID, name, email, phone, user type, and number of registered events.</p>
     *
     * @param user the user whose details should be displayed
     */
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

    /**
     * Handles the delete profile action for a user.
     *
     * <p>Shows a confirmation dialog before proceeding with deletion. The dialog
     * message adapts based on the user's type (Organizer, Entrant, etc.). Warns
     * that the action cannot be undone.</p>
     *
     * @param user the user profile to delete
     */
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

    /**
     * Handles the revoke organizer action for a user.
     *
     * <p>Shows a confirmation dialog before proceeding with revoking organizer
     * privileges. This operation will remove the user from the organizers collection
     * and change their user type to Entrant.</p>
     *
     * @param user the user whose organizer privileges should be revoked
     */
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

    /**
     * Deletes a user profile after confirmation, with cascade deletion for organizers.
     *
     * <p>If the user is an organizer, first deletes the organizer document from the
     * organizers collection before deleting the user profile. This ensures proper
     * cleanup of organizer-specific data.</p>
     *
     * <p>Shows a progress bar during the operation and displays appropriate toast
     * messages for success or failure.</p>
     *
     * @param user the user profile to delete
     */
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

    /**
     * Deletes the user profile from the users collection in Firestore.
     *
     * <p>Removes the user from both local data structures and the RecyclerView on
     * success. Updates the UI and displays appropriate toast messages.</p>
     *
     * @param user the user profile to delete
     */
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

    /**
     * Revokes organizer privileges from a user.
     *
     * <p>This method performs two operations:</p>
     * <ol>
     *     <li>Deletes the user's organizer document from the organizers collection</li>
     *     <li>Updates the user's type from Organizer to Entrant in the users collection</li>
     * </ol>
     *
     * <p>Shows a progress bar during the operation and displays appropriate toast
     * messages for success or failure. Updates the UI to reflect the user's new type.</p>
     *
     * @param user the user whose organizer privileges should be revoked
     */
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