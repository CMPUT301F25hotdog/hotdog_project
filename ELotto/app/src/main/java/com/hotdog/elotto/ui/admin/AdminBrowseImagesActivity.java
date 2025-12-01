package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminImageAdapter;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;
import com.hotdog.elotto.model.User;
import com.hotdog.elotto.helpers.UserType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Activity for administrators to browse and manage event poster images.
 *
 * <p>This activity provides administrators with the ability to view all event poster
 * images in a grid layout, search/filter images by event name, and delete poster images
 * from events. Access is restricted to users with Administrator privileges.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Grid layout display of event poster images</li>
 *     <li>Real-time search filtering by event name</li>
 *     <li>Image deletion with confirmation dialog</li>
 *     <li>Optimistic UI updates with background Firestore synchronization</li>
 *     <li>Offline support - changes sync when connection is restored</li>
 *     <li>Total image count display</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseImagesActivity extends AppCompatActivity implements AdminImageAdapter.OnImageActionListener {

    private static final String TAG = "AdminBrowseImages";
    // Device ID check disabled for testing
    // private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    /**
     * RecyclerView for displaying event poster images in a grid.
     */
    private RecyclerView recyclerViewImages;

    /**
     * TextView displaying the total number of images.
     */
    private TextView tvTotalImages;

    /**
     * TextView displayed when no images are found or match the search query.
     */
    private TextView tvNoImages;

    /**
     * ProgressBar shown during loading operations.
     */
    private ProgressBar progressBar;

    /**
     * EditText for search/filter input.
     */
    private EditText etSearchImages;

    /**
     * ImageView button for navigating back.
     */
    private ImageView btnBack;

    /**
     * Adapter for binding event image data to the RecyclerView.
     */
    private AdminImageAdapter adapter;

    /**
     * Repository for event data access operations.
     */
    private EventRepository eventRepository;

    /**
     * Firestore database instance for direct image deletion operations.
     */
    private FirebaseFirestore db;

    /**
     * Handler for posting UI updates on the main thread.
     */
    private Handler mainHandler;

    /**
     * Complete list of events that have poster images.
     */
    private List<Event> eventsWithImages = new ArrayList<>();

    /**
     * Filtered list of events based on search query.
     */
    private List<Event> filteredEvents = new ArrayList<>();

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

                setContentView(R.layout.activity_admin_browse_images);

                // Hide action bar
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }

                initializeViews();
                setupRecyclerView();
                setupSearchBox();
                setupBackButton();
                loadImages();
            }
        });


    }

    /**
     * Initializes all view components, repositories, and handlers.
     *
     * <p>Binds UI elements by their IDs, creates repository and database instances,
     * and initializes the main thread handler for UI updates.</p>
     */
    private void initializeViews() {
        recyclerViewImages = findViewById(R.id.rv_admin_images);
        tvTotalImages = findViewById(R.id.tv_total_images);
        tvNoImages = findViewById(R.id.tv_no_images);
        progressBar = findViewById(R.id.progress_bar);
        etSearchImages = findViewById(R.id.et_search_images);
        btnBack = findViewById(R.id.btn_back);

        eventRepository = new EventRepository();
        db = FirebaseFirestore.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "FirebaseFirestore instance: " + (db != null ? "initialized" : "NULL"));
    }

    /**
     * Sets up the back button click listener to finish the activity.
     */
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Sets up the RecyclerView with a grid layout and adapter.
     *
     * <p>Uses a GridLayoutManager with 2 columns to display images in a grid format.
     * Sets this activity as the action listener for handling image deletion.</p>
     */
    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(filteredEvents, this);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewImages.setAdapter(adapter);
    }

    /**
     * Sets up the search functionality with real-time text filtering.
     *
     * <p>Adds a TextWatcher to the search EditText that filters images as the
     * user types, providing instant search results.</p>
     */
    private void setupSearchBox() {
        etSearchImages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Filters events based on the search query.
     *
     * <p>Performs a case-insensitive search across event names. If the query is
     * empty, displays all events with images. Updates the UI with filtered results.</p>
     *
     * @param query the search query string
     */
    private void filterImages(String query) {
        filteredEvents.clear();

        if (query.isEmpty()) {
            filteredEvents.addAll(eventsWithImages);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Event event : eventsWithImages) {
                if (event.getName() != null && event.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }
        }

        updateUI();
    }

    /**
     * Loads all events with poster images from Firestore.
     *
     * <p>Retrieves all events from the repository, filters to include only those
     * with non-null and non-empty poster image URLs, and updates the UI. Shows
     * a progress bar during loading and logs detailed information about events
     * with and without images.</p>
     */
    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoImages.setVisibility(View.GONE);

        eventRepository.getAllEvents(new com.hotdog.elotto.callback.FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                eventsWithImages.clear();
                filteredEvents.clear();

                Log.d(TAG, "Total events retrieved: " + events.size());

                // Filter events that have poster images
                for (Event event : events) {
                    String imageUrl = event.getPosterImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Log.d(TAG, "Event with image - ID: " + event.getId() + ", Name: " + event.getName());
                        eventsWithImages.add(event);
                    } else {
                        Log.d(TAG, "Event without image - Name: " + event.getName());
                    }
                }

                filteredEvents.addAll(eventsWithImages);
                Log.d(TAG, "Events with images: " + eventsWithImages.size());
                updateUI();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoImages.setVisibility(View.VISIBLE);
                tvNoImages.setText("Error loading images: " + errorMessage);
                Toast.makeText(AdminBrowseImagesActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading events: " + errorMessage);
            }
        });
    }

    /**
     * Updates the UI based on current filtered events data.
     *
     * <p>Posts the update operation on the main thread to ensure thread safety.
     * Updates the total images count, shows/hides the "no images" message
     * appropriately, and notifies the adapter of data changes.</p>
     */
    private void updateUI() {
        mainHandler.post(() -> {
            tvTotalImages.setText("Total Images: " + filteredEvents.size());

            if (filteredEvents.isEmpty()) {
                tvNoImages.setVisibility(View.VISIBLE);
                tvNoImages.setText("No images found");
            } else {
                tvNoImages.setVisibility(View.GONE);
            }

            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Handles the delete image action for an event.
     *
     * <p>Shows a confirmation dialog before proceeding with deletion to prevent
     * accidental deletions.</p>
     *
     * @param event the event whose poster image should be deleted
     */
    @Override
    public void onDeleteImage(Event event) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete the poster image for \"" + event.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the poster image from an event with optimistic UI updates.
     *
     * <p>This method implements optimistic UI updates by immediately removing the image
     * from the display and local data structures, then syncing the change to Firestore
     * in the background. This approach provides instant feedback to the user and works
     * even when offline - changes will sync automatically when connection is restored.</p>
     *
     * <p>The deletion process:</p>
     * <ol>
     *     <li>Validates that the event has a valid ID</li>
     *     <li>Immediately updates local data and UI (optimistic update)</li>
     *     <li>Attempts to sync deletion to Firestore in background</li>
     *     <li>Logs detailed information about the operation</li>
     * </ol>
     *
     * <p><b>Note:</b> If Firestore sync fails (e.g., due to no internet connection),
     * the change is saved locally and will automatically sync when connection is
     * restored due to Firestore's offline persistence.</p>
     *
     * @param event the event whose poster image should be deleted
     */
    private void deleteImage(Event event) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "=== DELETE IMAGE STARTED ===");
        Log.d(TAG, "Event ID: " + event.getId());
        Log.d(TAG, "Event Name: " + event.getName());

        if (event.getId() == null || event.getId().isEmpty()) {
            Log.e(TAG, "CRITICAL ERROR: Event ID is NULL or EMPTY!");
            Toast.makeText(this, "Error: Event ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar briefly
        progressBar.setVisibility(View.VISIBLE);

        // OPTIMISTIC UPDATE: Update UI immediately
        event.setPosterImageUrl(null);
        eventsWithImages.remove(event);
        filteredEvents.remove(event);

        Log.d(TAG, "Removed from local lists");

        // Update UI
        updateUI();

        // Hide progress bar after a short delay
        mainHandler.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "UI updated - image removed from display");
        }, 500);

        // Try to update Firestore in background (will sync when online)
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("posterImageUrl", "");

        Log.d(TAG, "Attempting background Firestore update for: events/" + event.getId());

        db.collection("events")
                .document(event.getId())
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "=== FIRESTORE SYNC SUCCESS ===");
                    Log.d(TAG, "Image deletion synced to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "=== FIRESTORE SYNC FAILED (will retry when online) ===");
                    Log.e(TAG, "Error: " + e.getMessage());
                    Log.e(TAG, "Note: Changes saved locally and will sync when internet is available");
                });

        Log.d(TAG, "========================================");
    }
}