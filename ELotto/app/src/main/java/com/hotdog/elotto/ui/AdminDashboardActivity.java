package com.hotdog.elotto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hotdog.elotto.R;

/**
 * Admin Dashboard Activity
 * Displays overview statistics and provides navigation to admin management screens
 */
public class AdminDashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView totalEventsCount;
    private TextView totalUsersCount;
    private TextView totalImagesCount;
    private TextView notificationsCount;

    private MaterialCardView browseEventsCard;
    private MaterialCardView browseProfilesCard;
    private MaterialCardView browseImagesCard;
    private MaterialCardView notificationLogsCard;

    // Back button
    private ImageButton backButton;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load data
        loadDashboardData();
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        // Back button
        backButton = findViewById(R.id.back_button);

        // Overview count TextViews
        totalEventsCount = findViewById(R.id.total_events_count);
        totalUsersCount = findViewById(R.id.total_users_count);
        totalImagesCount = findViewById(R.id.total_images_count);
        notificationsCount = findViewById(R.id.notifications_count);

        // Management cards
        browseEventsCard = findViewById(R.id.browse_events_card);
        browseProfilesCard = findViewById(R.id.browse_profiles_card);
        browseImagesCard = findViewById(R.id.browse_images_card);
        notificationLogsCard = findViewById(R.id.notification_logs_card);
    }

    /**
     * Set up click listeners for all navigation cards
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Browse Events
        browseEventsCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, BrowseEventsActivity.class);
            startActivity(intent);
        });

        // Browse Profiles
        browseProfilesCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, BrowseProfilesActivity.class);
            startActivity(intent);
        });

        // Browse Images
        browseImagesCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, BrowseImagesActivity.class);
            startActivity(intent);
        });

        // Notification Logs
        notificationLogsCard.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, NotificationLogsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load dashboard statistics from Firebase
     */
    private void loadDashboardData() {
        loadTotalEvents();
        loadTotalUsers();
        loadTotalImages();
        loadNotificationsCount();
    }

    /**
     * Load total events count from Firestore
     */
    private void loadTotalEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    totalEventsCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    totalEventsCount.setText("0");
                    showError("Failed to load events count");
                });
    }

    /**
     * Load total users count from Firestore
     */
    private void loadTotalUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    totalUsersCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    totalUsersCount.setText("0");
                    showError("Failed to load users count");
                });
    }

    /**
     * Load total images count from Firestore
     */
    private void loadTotalImages() {
        db.collection("images")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    totalImagesCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    totalImagesCount.setText("0");
                    showError("Failed to load images count");
                });
    }

    /**
     * Load notifications count from Firestore
     */
    private void loadNotificationsCount() {
        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    notificationsCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    notificationsCount.setText("0");
                    showError("Failed to load notifications count");
                });
    }

    /**
     * Show error message
     * @param message Error message to display
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadDashboardData();
    }
}