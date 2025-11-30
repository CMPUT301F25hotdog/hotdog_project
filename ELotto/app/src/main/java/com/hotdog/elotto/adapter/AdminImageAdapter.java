package com.hotdog.elotto.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Event;

import java.util.List;

/**
 * Adapter for displaying event poster images in admin browse screen.
 * Supports both Firebase Storage URLs and Base64-encoded images.
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    private static final String TAG = "AdminImageAdapter";
    private final List<Event> events;
    private final OnImageActionListener listener;

    public interface OnImageActionListener {
        void onDeleteImage(Event event);
    }

    public AdminImageAdapter(List<Event> events, OnImageActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvEventName;
        private final ImageView ivDelete;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(Event event, OnImageActionListener listener) {
            tvEventName.setText(event.getName() != null ? event.getName() : "[No Name]");

            String imageData = event.getPosterImageUrl();

            if (imageData != null && !imageData.isEmpty()) {
                // Check if it's a Base64 string or a URL
                if (imageData.startsWith("http://") || imageData.startsWith("https://")) {
                    // It's a Firebase Storage URL - use Glide
                    Log.d(TAG, "Loading Firebase Storage URL for: " + event.getName());
                    Glide.with(itemView.getContext())
                            .load(imageData)
                            .placeholder(R.drawable.image_24px)
                            .error(R.drawable.image_24px)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(ivPoster);
                } else {
                    // It's a Base64 string - decode and display
                    Log.d(TAG, "Loading Base64 image for: " + event.getName());
                    try {
                        // Remove data URI prefix if present (e.g., "data:image/jpeg;base64,")
                        String base64String = imageData;
                        if (imageData.contains(",")) {
                            base64String = imageData.split(",")[1];
                        }

                        // Decode Base64 to byte array
                        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

                        // Convert byte array to Bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        if (bitmap != null) {
                            ivPoster.setImageBitmap(bitmap);
                            Log.d(TAG, "Successfully loaded Base64 image for: " + event.getName());
                        } else {
                            Log.e(TAG, "Failed to decode Base64 to Bitmap for: " + event.getName());
                            ivPoster.setImageResource(R.drawable.image_24px);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error decoding Base64 image: " + e.getMessage());
                        ivPoster.setImageResource(R.drawable.image_24px);
                    }
                }
            } else {
                Log.d(TAG, "No image data for event: " + event.getName());
                ivPoster.setImageResource(R.drawable.image_24px);
            }

            ivDelete.setOnClickListener(v -> listener.onDeleteImage(event));
        }
    }
}