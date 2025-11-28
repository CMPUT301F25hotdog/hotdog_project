package com.hotdog.elotto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Event;

import java.util.List;

/**
 * Adapter for displaying event poster images in admin browse screen.
 * Uses Glide for efficient image loading from Firebase Storage.
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

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

    // Made public to fix visibility scope warning
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

            // Load image using Glide with placeholder and error handling
            if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(event.getPosterImageUrl())
                        .placeholder(R.drawable.image_24px)  // Show while loading
                        .error(R.drawable.image_24px)        // Show if load fails
                        .centerCrop()                         // Scale image properly
                        .into(ivPoster);
            } else {
                // No image URL - show placeholder
                ivPoster.setImageResource(R.drawable.image_24px);
            }

            ivDelete.setOnClickListener(v -> listener.onDeleteImage(event));
        }
    }
}