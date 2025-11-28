package com.hotdog.elotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminImageAdapter;
import com.hotdog.elotto.model.Event;
import com.hotdog.elotto.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Browse Images Activity.
 * Implements US 03.06.01 (Browse images) and US 03.03.01 (Remove images).
 * Only accessible on device with ID: ded8763e1984cbfc
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminBrowseImagesActivity extends AppCompatActivity implements AdminImageAdapter.OnImageActionListener {

    private static final String ADMIN_DEVICE_ID = "ded8763e1984cbfc";

    private RecyclerView recyclerViewImages;
    private TextView tvTotalImages, tvNoImages;
    private ProgressBar progressBar;
    private Button btnDeleteImages;

    private AdminImageAdapter adapter;
    private EventRepository eventRepository;
    private List<Event> eventsWithImages = new ArrayList<>();

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

        initializeViews();
        setupRecyclerView();
        setupButton();
        loadImages();
    }

    private void initializeViews() {
        recyclerViewImages = findViewById(R.id.rv_admin_images);
        tvTotalImages = findViewById(R.id.tv_total_images);
        tvNoImages = findViewById(R.id.tv_no_images);
        progressBar = findViewById(R.id.progress_bar);
        btnDeleteImages = findViewById(R.id.btn_delete_images);

        eventRepository = new EventRepository();
    }

    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(eventsWithImages, this);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewImages.setAdapter(adapter);
    }

    private void setupButton() {
        btnDeleteImages.setOnClickListener(v -> {
            Toast.makeText(this, "Tap individual images to delete them", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoImages.setVisibility(View.GONE);

        eventRepository.getAllEvents(new com.hotdog.elotto.callback.FirestoreListCallback<Event>() {
            @Override
            public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                eventsWithImages.clear();

                // Filter events that have poster images
                for (Event event : events) {
                    if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
                        eventsWithImages.add(event);
                    }
                }

                updateUI();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvNoImages.setVisibility(View.VISIBLE);
                tvNoImages.setText("Error loading images: " + errorMessage);
                Toast.makeText(AdminBrowseImagesActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        tvTotalImages.setText("Total Images: " + eventsWithImages.size());

        if (eventsWithImages.isEmpty()) {
            tvNoImages.setVisibility(View.VISIBLE);
            tvNoImages.setText("No images found");
        } else {
            tvNoImages.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
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
        progressBar.setVisibility(View.VISIBLE);

        // Delete image from Firebase Storage if it exists
        String imageUrl = event.getPosterImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            // After storage deletion, update event to remove URL
                            updateEventAfterImageDeletion(event);
                        })
                        .addOnFailureListener(e -> {
                            // Even if storage deletion fails, update event
                            updateEventAfterImageDeletion(event);
                        });
            } catch (Exception e) {
                // If URL parsing fails, just update event
                updateEventAfterImageDeletion(event);
            }
        } else {
            updateEventAfterImageDeletion(event);
        }
    }

    private void updateEventAfterImageDeletion(Event event) {
        // Remove image URL from event
        event.setPosterImageUrl(null);

        eventRepository.updateEvent(event, new com.hotdog.elotto.callback.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                eventsWithImages.remove(event);
                updateUI();
                Toast.makeText(AdminBrowseImagesActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminBrowseImagesActivity.this, "Failed to update event: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}