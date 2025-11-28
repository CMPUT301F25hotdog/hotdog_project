package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.UserRepository;

/**
 * Admin Dashboard Activity - Main screen for admin functionality.
 * Only accessible on device with ID: ded8763e1984cbfc
 *
 * Provides navigation to:
 * - Browse Events (US 03.04.01)
 * - Browse Profiles (US 03.05.01)
 * - Browse Images (US 03.06.01)
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    private TextView tvTotalEvents, tvTotalUsers, tvTotalImages;
    private CardView cardBrowseEvents, cardBrowseProfiles, cardBrowseImages;

    private EventRepository eventRepository;
    private UserRepository userRepository;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL: Device ID check
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!ADMIN_DEVICE_ID.equals(deviceId)) {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        initializeViews();
        initializeRepositories();
        loadOverviewData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalImages = findViewById(R.id.tv_total_images);

        cardBrowseEvents = findViewById(R.id.card_browse_events);
        cardBrowseProfiles = findViewById(R.id.card_browse_profiles);
        cardBrowseImages = findViewById(R.id.card_browse_images);
    }

    private void initializeRepositories() {
        eventRepository = new EventRepository();
        userRepository = UserRepository.getInstance();
    }

    private void loadOverviewData() {
        // Load total events count
        eventRepository.getAllEvents(new com.hotdog.elotto.callback.FirestoreListCallback<com.hotdog.elotto.model.Event>() {
            @Override
            public void onSuccess(java.util.List<com.hotdog.elotto.model.Event> events) {
                int totalImages = 0;
                for (com.hotdog.elotto.model.Event event : events) {
                    if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
                        totalImages++;
                    }
                }
                tvTotalEvents.setText(String.valueOf(events.size()));
                tvTotalImages.setText(String.valueOf(totalImages));
            }

            @Override
            public void onError(String errorMessage) {
                tvTotalEvents.setText("0");
                tvTotalImages.setText("0");
            }
        });

        // Load total users count
        userRepository.getAllUsers(new com.hotdog.elotto.callback.FirestoreListCallback<com.hotdog.elotto.model.User>() {
            @Override
            public void onSuccess(java.util.List<com.hotdog.elotto.model.User> users) {
                tvTotalUsers.setText(String.valueOf(users.size()));
            }

            @Override
            public void onError(String errorMessage) {
                tvTotalUsers.setText("0");
            }
        });
    }

    private void setupClickListeners() {
        cardBrowseEvents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBrowseEventsActivity.class);
            startActivity(intent);
        });

        cardBrowseProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBrowseProfilesActivity.class);
            startActivity(intent);
        });

        cardBrowseImages.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBrowseImagesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOverviewData(); // Refresh data when returning to dashboard
    }
}