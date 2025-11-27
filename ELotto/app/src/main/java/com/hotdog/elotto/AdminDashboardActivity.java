package com.hotdog.elotto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Admin Dashboard - Shows counts and navigation
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private static final String ADMIN_DEVICE_ID = "39a4f2a6299a89c5";

    private TextView textTotalEvents, textTotalUsers, textTotalNotifications;
    private CardView cardBrowseEvents, cardBrowseProfiles, cardNotificationLogs;
    private ImageView btnRefresh;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Admin check
        if (!isAdminDevice()) {
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        loadDashboardData();
        setupClickListeners();
    }

    private boolean isAdminDevice() {
        String deviceId = android.provider.Settings.Secure.getString(
                getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );
        Log.d(TAG, "Current device ID: " + deviceId);
        return ADMIN_DEVICE_ID.equals(deviceId);
    }

    private void initViews() {
        textTotalEvents = findViewById(R.id.text_total_events);
        textTotalUsers = findViewById(R.id.text_total_users);
        textTotalNotifications = findViewById(R.id.text_total_notifications);

        cardBrowseEvents = findViewById(R.id.card_browse_events);
        cardBrowseProfiles = findViewById(R.id.card_browse_profiles);
        cardNotificationLogs = findViewById(R.id.card_notification_logs);

        btnRefresh = findViewById(R.id.btn_refresh);
    }

    private void loadDashboardData() {
        loadEventsCount();
        loadUsersCount();
        loadNotificationsCount();
    }

    private void loadEventsCount() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    textTotalEvents.setText(String.valueOf(count));
                    Log.d(TAG, "Events count: " + count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    textTotalEvents.setText("0");
                });
    }

    private void loadUsersCount() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    textTotalUsers.setText(String.valueOf(count));
                    Log.d(TAG, "Users count: " + count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users", e);
                    textTotalUsers.setText("0");
                });
    }

    private void loadNotificationsCount() {
        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    textTotalNotifications.setText(String.valueOf(count));
                    Log.d(TAG, "Notifications count: " + count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications", e);
                    textTotalNotifications.setText("0");
                });
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            loadDashboardData();
        });

        cardBrowseEvents.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, BrowseEventsActivity.class));
        });

        cardBrowseProfiles.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, BrowseProfilesActivity.class));
        });

        cardNotificationLogs.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, BrowseNotificationLogsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}