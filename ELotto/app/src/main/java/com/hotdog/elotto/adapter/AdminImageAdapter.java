package com.hotdog.elotto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.ImageData;

import java.util.List;

/**
 * Adapter for displaying images in a grid layout in the admin browse images screen.
 * @author Your Name
 * @version 1.0.0
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    private Context context;
    private List<ImageData> images;
    private OnImageDeleteListener listener;

    /**
     * Interface for handling image deletion
     */
    public interface OnImageDeleteListener {
        void onDeleteImage(ImageData imageData);
    }

    /**
     * Constructor
     * @param context Application context
     * @param images List of images to display
     * @param listener Listener for image deletion
     */
    public AdminImageAdapter(Context context, List<ImageData> images, OnImageDeleteListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageData imageData = images.get(position);
        holder.bind(imageData);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * ViewHolder class for image items
     */
    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView adminImage, btnDeleteImage;
        TextView imageEventName, imageOwnerName, imageType;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            adminImage = itemView.findViewById(R.id.admin_image);
            imageEventName = itemView.findViewById(R.id.image_event_name);
            imageOwnerName = itemView.findViewById(R.id.image_owner_name);
            imageType = itemView.findViewById(R.id.image_type);
            btnDeleteImage = itemView.findViewById(R.id.btn_delete_image);
        }

        /**
         * Bind image data to views
         * @param imageData ImageData object to display
         */
        public void bind(ImageData imageData) {
            imageEventName.setText(imageData.getEventName());
            imageOwnerName.setText("By " + imageData.getOwnerName());
            imageType.setText("[" + imageData.getType() + "]");

            // Load image using Glide or Picasso
            // TODO: Implement image loading
            // if (imageData.getUrl() != null && !imageData.getUrl().isEmpty()) {
            //     Glide.with(context)
            //         .load(imageData.getUrl())
            //         .placeholder(R.drawable.baseline_image_24)
            //         .error(R.drawable.baseline_image_24)
            //         .centerCrop()
            //         .into(adminImage);
            // }

            // Set delete button click listener
            btnDeleteImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteImage(imageData);
                }
            });

            // Optional: Click to view full image
            itemView.setOnClickListener(v -> {
                // TODO: Show full image in dialog or new activity
                // Intent intent = new Intent(context, ImageViewerActivity.class);
                // intent.putExtra("imageUrl", imageData.getUrl());
                // context.startActivity(intent);
            });
        }
    }
}