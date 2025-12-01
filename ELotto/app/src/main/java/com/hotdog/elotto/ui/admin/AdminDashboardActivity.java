package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.repository.UserRepository;

import java.util.function.Consumer;

/**
 * Main dashboard activity for administrators to access system management features.
 *
 * <p>This activity serves as the central hub for administrative operations, providing
 * overview statistics and navigation to various management screens. Access is restricted
 * to users with Administrator privileges.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Overview statistics showing total events, users, and images</li>
 *     <li>Navigation cards to browse events management screen</li>
 *     <li>Navigation cards to browse profiles management screen</li>
 *     <li>Navigation cards to browse images management screen</li>
 *     <li>Navigation cards to browse notifications management screen</li>
 *     <li>Automatic data refresh when returning to dashboard</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminDashboardActivity extends AppCompatActivity {

    // Device ID check temporarily disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    /**
     * TextView displaying the total number of events in the system.
     */
    private TextView tvTotalEvents;

    /**
     * TextView displaying the total number of users in the system.
     */
    private TextView tvTotalUsers;

    /**
     * TextView displaying the total number of event poster images in the system.
     */
    private TextView tvTotalImages;

    /**
     * CardView for navigating to the browse events screen.
     */
    private CardView cardBrowseEvents;

    /**
     * CardView for navigating to the browse profiles screen.
     */
    private CardView cardBrowseProfiles;

    /**
     * CardView for navigating to the browse images screen.
     */
    private CardView cardBrowseImages;

    /**
     * CardView for navigating to the browse notifications screen.
     */
    private CardView cardBrowseNotifications;

    /**
     * Repository for event data access operations.
     */
    private EventRepository eventRepository;

    /**
     * Repository for user data access operations.
     */
    private UserRepository userRepository;

    /**
     * Called when the activity is starting.
     *
     * <p>Verifies that the current user has Administrator privileges before
     * initializing the activity. If access is denied, displays a toast message
     * and finishes the activity. Hides the action bar to prevent duplicate titles.</p>
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

                setContentView(R.layout.activity_admin_dashboard);

                // HIDE ACTION BAR to prevent duplicate title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }

                initializeViews();
                initializeRepositories();
                loadOverviewData();
                setupClickListeners();
            }
        });
    }

    /**
     * Initializes all view components.
     *
     * <p>Binds UI elements including TextViews for statistics display and
     * CardViews for navigation buttons.</p>
     */
    private void initializeViews() {
        tvTotalEvents = findViewById(R.id.tv_total_events);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalImages = findViewById(R.id.tv_total_images);

        cardBrowseEvents = findViewById(R.id.card_browse_events);
        cardBrowseProfiles = findViewById(R.id.card_browse_profiles);
        cardBrowseImages = findViewById(R.id.card_browse_images);
        cardBrowseNotifications = findViewById(R.id.card_browse_notifications);
    }

    /**
     * Initializes repository instances for data access.
     *
     * <p>Creates a new EventRepository instance and obtains the singleton
     * UserRepository instance.</p>
     */
    private void initializeRepositories() {
        eventRepository = new EventRepository();
        userRepository = UserRepository.getInstance();
    }

    /**
     * Loads overview statistics data from Firestore.
     *
     * <p>This method loads and displays three key statistics:</p>
     * <ul>
     *     <li>Total number of events in the system</li>
     *     <li>Total number of users in the system</li>
     *     <li>Total number of events with poster images</li>
     * </ul>
     *
     * <p>If any data load fails, displays "0" as the default value. The image
     * count is calculated by iterating through all events and counting those
     * with non-null, non-empty poster image URLs.</p>
     */
    private void loadOverviewData() {
        // Load total events count
        eventRepository
                .getAllEvents(new com.hotdog.elotto.callback.FirestoreListCallback<com.hotdog.elotto.model.Event>() {
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
        userRepository
                .getAllUsers(new com.hotdog.elotto.callback.FirestoreListCallback<com.hotdog.elotto.model.User>() {
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

    /**
     * Sets up click listeners for all navigation cards.
     *
     * <p>Each card launches the appropriate admin management activity when clicked:</p>
     * <ul>
     *     <li>Browse Events card → AdminBrowseEventsActivity</li>
     *     <li>Browse Profiles card → AdminBrowseProfilesActivity</li>
     *     <li>Browse Images card → AdminBrowseImagesActivity</li>
     *     <li>Browse Notifications card → AdminBrowseNotificationsActivity</li>
     * </ul>
     */
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

        cardBrowseNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBrowseNotificationsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Called when the activity is becoming visible to the user.
     *
     * <p>Overrides the default onResume behavior to refresh overview statistics
     * whenever the activity returns to the foreground, ensuring displayed data
     * remains current after visiting other admin screens.</p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadOverviewData(); // Refresh data when returning to dashboard
    }
}