package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Browse Images Activity.
 * Implements US 03.06.01 (Browse images) and US 03.03.01 (Remove images).
 * Only accessible on device with ID: ded8763e1984cbfc
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseImagesActivity extends AppCompatActivity implements AdminImageAdapter.OnImageActionListener {

    private static final String TAG = "AdminBrowseImages";
    private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    private RecyclerView recyclerViewImages;
    private TextView tvTotalImages, tvNoImages;
    private ProgressBar progressBar;
    private EditText etSearchImages;

    private AdminImageAdapter adapter;
    private EventRepository eventRepository;
    private FirebaseFirestore db;
    private Handler mainHandler;
    private List<Event> eventsWithImages = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

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

        setContentView(R.layout.activity_admin_browse_images);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        setupSearchBox();
        loadImages();
    }

    private void initializeViews() {
        recyclerViewImages = findViewById(R.id.rv_admin_images);
        tvTotalImages = findViewById(R.id.tv_total_images);
        tvNoImages = findViewById(R.id.tv_no_images);
        progressBar = findViewById(R.id.progress_bar);
        etSearchImages = findViewById(R.id.et_search_images);

        eventRepository = new EventRepository();
        db = FirebaseFirestore.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "FirebaseFirestore instance: " + (db != null ? "initialized" : "NULL"));
    }

    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(filteredEvents, this);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewImages.setAdapter(adapter);
    }

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