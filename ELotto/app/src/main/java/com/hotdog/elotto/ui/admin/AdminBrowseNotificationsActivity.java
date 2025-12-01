package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminNotificationAdapter;
import com.hotdog.elotto.model.Notification;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.helpers.UserType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Activity for administrators to browse and manage all system notifications.
 *
 * <p>This activity provides administrators with the ability to view all notifications
 * sent to users across the system, search/filter notifications by message, title, or
 * event ID, view detailed notification information, and delete notifications. Access
 * is restricted to users with Administrator privileges.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>View all notifications from all users in the system</li>
 *     <li>Real-time search filtering across message, title, and event ID</li>
 *     <li>Notification details dialog showing comprehensive information</li>
 *     <li>Notification deletion with confirmation dialog</li>
 *     <li>Automatic sorting by timestamp (newest first)</li>
 *     <li>Total notification count display</li>
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
public class AdminBrowseNotificationsActivity extends AppCompatActivity
        implements AdminNotificationAdapter.OnNotificationActionListener {

    private static final String TAG = "AdminBrowseNotifications";

    /**
     * RecyclerView for displaying the list of notifications.
     */
    private RecyclerView recyclerViewNotifications;

    /**
     * EditText for search/filter input.
     */
    private EditText etSearchNotifications;

    /**
     * TextView displaying the total number of notifications.
     */
    private TextView tvTotalNotifications;

    /**
     * TextView displayed when no notifications are found or match the search query.
     */
    private TextView tvNoNotifications;

    /**
     * ProgressBar shown during loading operations.
     */
    private ProgressBar progressBar;

    /**
     * ImageView button for navigating back.
     */
    private ImageView btnBack;

    /**
     * Adapter for binding notification data to the RecyclerView.
     */
    private AdminNotificationAdapter adapter;

    /**
     * Firestore database instance for notification operations.
     */
    private FirebaseFirestore db;

    /**
     * Complete list of all notifications loaded from Firestore.
     */
    private List<Notification> allNotifications = new ArrayList<>();

    /**
     * Filtered list of notifications based on search query.
     */
    private List<Notification> filteredNotifications = new ArrayList<>();

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

                setContentView(R.layout.activity_admin_browse_notifications);

                // Hide action bar
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }

                initializeViews();
                setupRecyclerView();
                setupSearch();
                setupBackButton();
                loadNotifications();
            }
        });
    }

    /**
     * Initializes all view components and the Firestore database instance.
     *
     * <p>Binds UI elements by their IDs and creates a Firestore instance for
     * notification data access.</p>
     */
    private void initializeViews() {
        recyclerViewNotifications = findViewById(R.id.rv_admin_notifications);
        etSearchNotifications = findViewById(R.id.et_search_notifications);
        tvTotalNotifications = findViewById(R.id.tv_total_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);

        db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets up the back button click listener to finish the activity.
     */
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     *
     * <p>Creates an AdminNotificationAdapter with the filtered notifications list
     * and sets this activity as the action listener for handling notification actions.</p>
     */
    private void setupRecyclerView() {
        adapter = new AdminNotificationAdapter(filteredNotifications, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

    /**
     * Sets up the search functionality with real-time text filtering.
     *
     * <p>Adds a TextWatcher to the search EditText that filters notifications as
     * the user types, providing instant search results.</p>
     */
    private void setupSearch() {
        etSearchNotifications.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotifications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Loads all notifications from all users in the Firestore database.
     *
     * <p>This method queries the notifications collection, parses notification arrays
     * from each user document, and constructs Notification objects with proper error
     * handling for malformed data. Notifications are automatically sorted by timestamp
     * in descending order (newest first).</p>
     *
     * <p>Handles multiple field name variations for backward compatibility (e.g., both
     * "isRead" and "read" fields). Includes optional fields like eventTitle and
     * eventImageUrl if present.</p>
     */
    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);

        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allNotifications.clear();
                    filteredNotifications.clear();

                    Log.d(TAG, "Total user documents retrieved: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        try {
                            // The field "notifications" is an array of maps
                            List<Map<String, Object>> notificationsList = (List<Map<String, Object>>) doc.get("notifications");

                            if (notificationsList != null) {
                                for (Map<String, Object> notifMap : notificationsList) {
                                    try {
                                        Notification notification = new Notification();
                                        notification.setUuid((String) notifMap.get("uuid"));
                                        notification.setEventId((String) notifMap.get("eventId"));
                                        notification.setMessage((String) notifMap.get("message"));
                                        notification.setTitle((String) notifMap.get("title"));

                                        // Handle read field safely (check both "read" and "isRead")
                                        Boolean isRead = (Boolean) notifMap.get("isRead");
                                        if (isRead == null) {
                                            isRead = (Boolean) notifMap.get("read");
                                        }
                                        notification.setRead(isRead != null ? isRead : false);

                                        notification.setTimestamp(
                                                (com.google.firebase.Timestamp) notifMap.get("timestamp"));
                                        notification.setUserId(userId); // Store the document ID (userId) for context

                                        // Handle new fields from dev branch if present
                                        if (notifMap.containsKey("eventTitle")) {
                                            notification.setEventTitle((String) notifMap.get("eventTitle"));
                                        }
                                        if (notifMap.containsKey("eventImageUrl")) {
                                            notification.setEventImageUrl((String) notifMap.get("eventImageUrl"));
                                        }

                                        allNotifications.add(notification);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing individual notification map for user: " + userId, e);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notifications array for user: " + userId, e);
                        }
                    }

                    // Sort by timestamp descending (since we can't do it in the query anymore)
                    allNotifications.sort((n1, n2) -> {
                        if (n1.getTimestamp() == null || n2.getTimestamp() == null)
                            return 0;
                        return n2.getTimestamp().compareTo(n1.getTimestamp());
                    });

                    filteredNotifications.addAll(allNotifications);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    tvNoNotifications.setText("Error loading notifications");
                    Toast.makeText(this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading notifications", e);
                });
    }

    /**
     * Filters notifications based on the search query.
     *
     * <p>Performs a case-insensitive search across notification message, title, and
     * event ID fields. If the query is empty, displays all notifications. Updates
     * the UI with the filtered results.</p>
     *
     * @param query the search query string
     */
    private void filterNotifications(String query) {
        filteredNotifications.clear();

        if (query.isEmpty()) {
            filteredNotifications.addAll(allNotifications);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Notification notification : allNotifications) {
                String message = notification.getMessage() != null ? notification.getMessage().toLowerCase() : "";
                String title = notification.getTitle() != null ? notification.getTitle().toLowerCase() : "";
                String eventId = notification.getEventId() != null ? notification.getEventId().toLowerCase() : "";

                if (message.contains(lowerCaseQuery) || title.contains(lowerCaseQuery)
                        || eventId.contains(lowerCaseQuery)) {
                    filteredNotifications.add(notification);
                }
            }
        }

        updateUI();
    }

    /**
     * Updates the UI based on current filtered notifications data.
     *
     * <p>Updates the total notifications count, shows/hides the "no notifications"
     * message appropriately, and notifies the adapter of data changes.</p>
     */
    private void updateUI() {
        tvTotalNotifications.setText("Total Notifications: " + filteredNotifications.size());

        if (filteredNotifications.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            tvNoNotifications
                    .setText(allNotifications.isEmpty() ? "No notifications found" : "No matching notifications");
        } else {
            tvNoNotifications.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Handles the notification click action.
     *
     * <p>Displays an AlertDialog with comprehensive notification details including
     * event ID, message, user ID, read/unread status, and formatted timestamp.</p>
     *
     * @param notification the notification that was clicked
     */
    @Override
    public void onNotificationClick(Notification notification) {
        // Show notification details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(notification.getTitle() != null ? notification.getTitle() : "Notification Details");

        StringBuilder details = new StringBuilder();
        details.append("Event ID: ").append(notification.getEventId() != null ? notification.getEventId() : "N/A")
                .append("\n\n");
        details.append("Message: ").append(notification.getMessage() != null ? notification.getMessage() : "N/A")
                .append("\n\n");
        details.append("User ID: ").append(notification.getUserId() != null ? notification.getUserId() : "N/A")
                .append("\n\n");
        details.append("Status: ").append(notification.isRead() ? "Read" : "Unread").append("\n\n");
        details.append("Timestamp: ").append(notification.getFormattedTimestamp());

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    /**
     * Handles the delete notification action.
     *
     * <p>Shows a confirmation dialog before proceeding with deletion to prevent
     * accidental deletions.</p>
     *
     * @param notification the notification to delete
     */
    @Override
    public void onDeleteNotification(Notification notification) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Notification")
                .setMessage("Are you sure you want to delete this notification?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNotification(notification))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a notification from the user's notification array in Firestore.
     *
     * <p>This method removes a specific notification from the notifications array
     * field in the user's notification document using Firestore's arrayRemove
     * operation. The entire notification object (as a map) is removed from the array.</p>
     *
     * <p>Validates that the notification has both a user ID and UUID before attempting
     * deletion. Shows a progress bar during the operation and displays appropriate
     * toast messages for success or failure.</p>
     *
     * @param notification the notification to delete from Firestore
     */
    private void deleteNotification(Notification notification) {
        if (notification.getUserId() == null || notification.getUuid() == null) {
            Toast.makeText(this, "Error: Missing User ID or Notification UUID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("uuid", notification.getUuid());
        notificationMap.put("eventId", notification.getEventId());
        notificationMap.put("message", notification.getMessage());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("isRead", notification.isRead());

        // Also add new fields if they are not null
        if (notification.getEventTitle() != null)
            notificationMap.put("eventTitle", notification.getEventTitle());
        if (notification.getEventImageUrl() != null)
            notificationMap.put("eventImageUrl", notification.getEventImageUrl());

        notificationMap.put("timestamp", notification.getTimestamp());

        db.collection("notifications")
                .document(notification.getUserId())
                .update("notifications", FieldValue.arrayRemove(notificationMap))
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    allNotifications.remove(notification);
                    filteredNotifications.remove(notification);
                    updateUI();
                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Notification deleted successfully: " + notification.getUuid());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to delete notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting notification", e);
                });
    }
}