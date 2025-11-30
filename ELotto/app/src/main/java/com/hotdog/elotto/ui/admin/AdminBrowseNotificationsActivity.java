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

/**
 * Admin Browse Notifications Activity.
 * Allows admin to view all notifications sent to users.
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseNotificationsActivity extends AppCompatActivity
        implements AdminNotificationAdapter.OnNotificationActionListener {

    private static final String TAG = "AdminBrowseNotifications";

    private RecyclerView recyclerViewNotifications;
    private EditText etSearchNotifications;
    private TextView tvTotalNotifications, tvNoNotifications;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private AdminNotificationAdapter adapter;
    private FirebaseFirestore db;
    private List<Notification> allNotifications = new ArrayList<>();
    private List<Notification> filteredNotifications = new ArrayList<>();

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Admin Access
        User currentUser = new User(this, true);
        if (currentUser.getType() != UserType.Administrator) {
            Toast.makeText(this, "Access Denied: Admin only", Toast.LENGTH_SHORT).show();
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

    private void initializeViews() {
        recyclerViewNotifications = findViewById(R.id.rv_admin_notifications);
        etSearchNotifications = findViewById(R.id.et_search_notifications);
        tvTotalNotifications = findViewById(R.id.tv_total_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);

        db = FirebaseFirestore.getInstance();
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminNotificationAdapter(filteredNotifications, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

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
                            List<Map<String, Object>> notificationsList = (List<Map<String, Object>>) doc
                                    .get("notifications");

                            if (notificationsList != null) {
                                for (Map<String, Object> notifMap : notificationsList) {
                                    try {
                                        Notification notification = new Notification();
                                        notification.setUuid((String) notifMap.get("uuid"));
                                        notification.setEventId((String) notifMap.get("eventId"));
                                        notification.setMessage((String) notifMap.get("message"));
                                        notification.setTitle((String) notifMap.get("title"));

                                        // Handle read field safely (default to false if missing or null)
                                        Boolean isRead = (Boolean) notifMap.get("read");
                                        notification.setRead(isRead != null ? isRead : false);

                                        notification.setTimestamp(
                                                (com.google.firebase.Timestamp) notifMap.get("timestamp"));
                                        notification.setUserId(userId); // Store the document ID (userId) for context

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

    private void deleteNotification(Notification notification) {
        if (notification.getUserId() == null || notification.getUuid() == null) {
            Toast.makeText(this, "Error: Missing User ID or Notification UUID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Reconstruct the map to remove it from the array
        // Note: FieldValue.arrayRemove requires an exact match of the element.
        // We need to be careful here. If the map has other fields we don't know about,
        // this might fail.
        // However, based on the user's description, we have all the fields.

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("uuid", notification.getUuid());
        notificationMap.put("eventId", notification.getEventId());
        notificationMap.put("message", notification.getMessage());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("read", notification.isRead());
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