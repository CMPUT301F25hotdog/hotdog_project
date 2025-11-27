package com.hotdog.elotto.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of notifications.
 * Handles styling for read/unread states.
 *
 * @author [Your Name]
 * @version 1.0
 * @since 2025-11-26
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView body;
        private ImageView dot;
        private ImageView icon;
        private View itemView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = itemView.findViewById(R.id.inboxNotificationTitle);
            body = itemView.findViewById(R.id.inboxNotificationBody);
            dot = itemView.findViewById(R.id.inboxNotificationDot);
            icon = itemView.findViewById(R.id.notificationIconImage);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        public void bind(Notification notification) {
            title.setText(notification.getTitle());
            body.setText(notification.getMessage());

            // Load event image if available
            if (notification.getEventImageUrl() != null && !notification.getEventImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(notification.getEventImageUrl())
                        .placeholder(R.drawable.baseline_image_24)
                        .error(R.drawable.baseline_image_24)
                        .into(icon);
            } else {
                icon.setImageResource(R.drawable.baseline_image_24);
            }

            if (!notification.isRead()) {
                // Unread: Light Blue background, Dot visible
                itemView.setBackgroundColor(Color.parseColor("#E1F5FE")); // Light Blue 50
                dot.setVisibility(View.VISIBLE);
            } else {
                // Read: White background, Dot gone
                itemView.setBackgroundColor(Color.WHITE);
                dot.setVisibility(View.GONE);
            }
        }
    }
}
