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
 * Adapter for displaying event poster images in the Admin's image management interface.
 *
 * <p>This adapter handles both Firebase Storage URLs and Base64-encoded images,
 * providing admins with the ability to browse and delete event poster images.
 * Uses Glide for efficient image loading and caching from Firebase Storage.</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Tatsat
 * @version 1.0
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    private static final String TAG = "AdminImageAdapter";

    /**
     * The list of events whose poster images are being displayed.
     */
    private final List<Event> events;

    /**
     * Listener for handling image deletion actions.
     */
    private final OnImageActionListener listener;

    /**
     * Callback interface for handling image deletion actions.
     *
     * <p>Implementing classes should handle the business logic for deleting
     * event poster images from Firebase Storage or local storage.</p>
     */
    public interface OnImageActionListener {
        /**
         * Called when the administrator clicks to delete an event's poster image.
         *
         * @param event the event whose poster image should be deleted
         */
        void onDeleteImage(Event event);
    }

    /**
     * Constructs a new AdminImageAdapter with the specified events and action listener.
     *
     * @param events the list of events with poster images to display
     * @param listener the callback listener for handling image deletion
     */
    public AdminImageAdapter(List<Event> events, OnImageActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the admin image item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new ImageViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds event image data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    /**
     * Returns the total number of events in the adapter.
     *
     * @return the size of the events list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder class for individual event poster image items.
     *
     * <p>Handles displaying event poster images from either Firebase Storage URLs
     * or Base64-encoded strings, with fallback to placeholder images when data
     * is unavailable or fails to load.</p>
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final TextView tvEventName;
        private final ImageView ivDelete;

        /**
         * Constructs a new ImageViewHolder and initializes all view references.
         *
         * @param itemView the root view of the image item layout
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        /**
         * Binds event poster image data to the view components and sets up click listeners.
         *
         * <p>This method handles three types of image data:</p>
         * <ul>
         *     <li>Firebase Storage URLs (http:// or https://) - loaded using Glide</li>
         *     <li>Base64-encoded strings - decoded and displayed as Bitmap</li>
         *     <li>Missing/null data - displays placeholder image</li>
         * </ul>
         *
         * <p>Base64 strings with data URI prefixes (e.g., "data:image/jpeg;base64,")
         * are automatically stripped before decoding.</p>
         *
         * @param event the event object containing the poster image data
         * @param listener the listener to handle image deletion action
         */
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