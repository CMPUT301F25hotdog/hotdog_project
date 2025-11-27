package com.hotdog.elotto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.adapter.AdminImageAdapter;
import com.hotdog.elotto.model.ImageData;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for browsing and managing all uploaded images as an administrator.
 * @author Your Name
 * @version 1.0.0
 */
public class BrowseImagesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnSearchImages;
    private TextView textTotalImagesCount;
    private RecyclerView recyclerImages;
    private LinearLayout emptyState;

    private AdminImageAdapter imageAdapter;
    private List<ImageData> allImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_images);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupRecyclerView();
        loadImages();
        setupClickListeners();
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSearchImages = findViewById(R.id.btn_search_images);
        textTotalImagesCount = findViewById(R.id.text_total_images_count);
        recyclerImages = findViewById(R.id.recycler_images);
        emptyState = findViewById(R.id.empty_state);
    }

    /**
     * Setup RecyclerView with adapter and grid layout
     */
    private void setupRecyclerView() {
        allImages = new ArrayList<>();

        imageAdapter = new AdminImageAdapter(this, allImages, imageData -> {
            showDeleteConfirmationDialog(imageData);
        });

        // Use GridLayoutManager for 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerImages.setLayoutManager(gridLayoutManager);
        recyclerImages.setAdapter(imageAdapter);
    }

    /**
     * Load all images from Firebase Storage and Firestore
     */
    private void loadImages() {
        // TODO: Implement Firebase query to load all images
        // This should load both event posters and profile pictures
        // Example:
        // FirebaseFirestore.getInstance().collection("images")
        //     .get()
        //     .addOnSuccessListener(queryDocumentSnapshots -> {
        //         allImages.clear();
        //         for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
        //             ImageData imageData = doc.toObject(ImageData.class);
        //             allImages.add(imageData);
        //         }
        //         updateUI();
        //     });

        // Placeholder: Show empty state for now
        updateUI();
    }

    /**
     * Update UI based on current data
     */
    private void updateUI() {
        textTotalImagesCount.setText("Total Images : " + allImages.size());

        if (allImages.isEmpty()) {
            recyclerImages.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerImages.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        imageAdapter.notifyDataSetChanged();
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSearchImages.setOnClickListener(v -> {
            // TODO: Implement search/filter dialog
            // Could show a dialog with options to filter by:
            // - Image type (event poster, profile picture)
            // - Owner
            // - Date uploaded
            // - Event name
        });
    }

    /**
     * Show confirmation dialog before deleting image
     * @param imageData Image to delete
     */
    private void showDeleteConfirmationDialog(ImageData imageData) {
        // TODO: Implement confirmation dialog
        // Use AlertDialog to confirm deletion
        // On confirmation, delete from Firebase Storage and Firestore:
        // 1. Delete from Storage:
        // FirebaseStorage.getInstance().getReferenceFromUrl(imageData.getUrl())
        //     .delete()
        //     .addOnSuccessListener(aVoid -> {
        //         // 2. Delete from Firestore:
        //         FirebaseFirestore.getInstance().collection("images")
        //             .document(imageData.getId())
        //             .delete()
        //             .addOnSuccessListener(aVoid2 -> {
        //                 allImages.remove(imageData);
        //                 updateUI();
        //                 Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
        //             });
        //     });
    }
}